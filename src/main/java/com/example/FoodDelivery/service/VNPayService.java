package com.example.FoodDelivery.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Wallet;
import com.example.FoodDelivery.domain.WalletTransaction;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VNPayService {
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;
    private final UserService userService;
    private final OrderRepository orderRepository;

    @Value("${vnpay.tmn_code:CTTVNP01}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash_secret:VNPAY_SECRET_KEY}")
    private String vnp_HashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_Url;

    public VNPayService(
            WalletService walletService,
            WalletTransactionService walletTransactionService,
            UserService userService,
            OrderRepository orderRepository) {
        this.walletService = walletService;
        this.walletTransactionService = walletTransactionService;
        this.userService = userService;
        this.orderRepository = orderRepository;
    }

    /**
     * Create VNPAY payment URL
     * 
     * @param order     Order to create payment for
     * @param ipAddress Client IP address
     * @param baseUrl   Base URL for callback (e.g., http://localhost:8080 or
     *                  http://server-ip:port)
     * @return Payment URL
     */
    public String createPaymentUrl(Order order, String ipAddress, String baseUrl) throws UnsupportedEncodingException {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);

        // Amount in VND * 100 (VNPay requires smallest unit)
        long amount = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();
        vnp_Params.put("vnp_Amount", String.valueOf(amount));

        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(order.getId())); // Order ID as transaction reference
        vnp_Params.put("vnp_OrderInfo", "Payment for order #" + order.getId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");

        // Generate dynamic callback URL based on request
        String callbackUrl = baseUrl + "/api/v1/payment/vnpay/callback";
        vnp_Params.put("vnp_ReturnUrl", callbackUrl);

        vnp_Params.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Payment expires in 15 minutes
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_Url + "?" + queryUrl;

        return paymentUrl;
    }

    /**
     * Process VNPAY callback
     * 
     * @param params Callback parameters from VNPAY
     * @return Processing result
     */
    @Transactional
    public Map<String, Object> processCallback(Map<String, String> params) throws IdInvalidException {
        Map<String, Object> result = new HashMap<>();

        // Log all params for debugging
        log.info("VNPAY callback params: {}", params);

        // Get response code
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef"); // Order ID
        String vnp_Amount = params.get("vnp_Amount");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");
        String vnp_SecureHash = params.get("vnp_SecureHash");

        // Verify secure hash
        // Create a copy of params to preserve original
        Map<String, String> paramsForHash = new HashMap<>(params);
        paramsForHash.remove("vnp_SecureHash");
        paramsForHash.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(paramsForHash.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = paramsForHash.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // URL encode the value like VNPAY does
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error("Error encoding field value: {}", e.getMessage());
                }
            }
        }

        String hashDataStr = hashData.toString();
        String calculatedHash = hmacSHA512(vnp_HashSecret, hashDataStr);

        log.info("Hash data (encoded): {}", hashDataStr);
        log.info("Calculated hash: {}", calculatedHash);
        log.info("Received hash: {}", vnp_SecureHash);

        // Compare case-insensitive since VNPAY might return uppercase or lowercase
        if (!calculatedHash.equalsIgnoreCase(vnp_SecureHash)) {
            log.error("Invalid secure hash verification failed!");
            result.put("success", false);
            result.put("message", "Invalid secure hash");
            return result;
        }

        log.info("Secure hash verified successfully!"); // Parse amount (divide by 100 to convert back to VND)
        BigDecimal amount = new BigDecimal(vnp_Amount).divide(new BigDecimal("100"));

        // Check response code
        // 00: Success
        // Other codes: Failed
        boolean isSuccess = "00".equals(vnp_ResponseCode);

        result.put("success", isSuccess);
        result.put("responseCode", vnp_ResponseCode);
        result.put("orderId", Long.valueOf(vnp_TxnRef));
        result.put("amount", amount);
        result.put("transactionNo", vnp_TransactionNo);

        if (isSuccess) {
            // Payment successful - add money to admin wallet
            User admin = getAdminUser();
            if (admin != null) {
                Wallet adminWallet = walletService.getWalletByUserId(admin.getId());
                if (adminWallet != null) {
                    walletService.addBalance(adminWallet.getId(), amount);

                    // Create wallet transaction for admin
                    WalletTransaction adminTransaction = WalletTransaction.builder()
                            .wallet(adminWallet)
                            .transactionType("VNPAY_RECEIVED")
                            .amount(amount)
                            .balanceAfter(adminWallet.getBalance())
                            .description("VNPAY payment received from order #" + vnp_TxnRef + ", Transaction: "
                                    + vnp_TransactionNo)
                            .relatedOrderId(Long.valueOf(vnp_TxnRef))
                            .status("SUCCESS")
                            .transactionDate(Instant.now())
                            .createdAt(Instant.now())
                            .build();
                    walletTransactionService.createWalletTransaction(adminTransaction);
                }
            }

            result.put("message", "Payment successful");
        } else {
            orderRepository.deleteById(Long.valueOf(vnp_TxnRef));
            result.put("message", "Payment failed with code: " + vnp_ResponseCode);
        }

        return result;
    }

    /**
     * Get admin user (role = ADMIN)
     * 
     * @return admin user
     */
    private User getAdminUser() {
        try {
            return userService.getUserByRoleName("ADMIN");
        } catch (Exception e) {
            log.error("Error getting admin user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Calculate HMAC SHA512
     * 
     * @param key  Secret key
     * @param data Data to hash
     * @return Hash string
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error calculating HMAC SHA512: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Get VNPAY response code description
     * 
     * @param responseCode Response code from VNPAY
     * @return Description
     */
    public String getResponseDescription(String responseCode) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("00", "Giao dịch thành công");
        descriptions.put("07",
                "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)");
        descriptions.put("09",
                "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng");
        descriptions.put("10",
                "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần");
        descriptions.put("11",
                "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch");
        descriptions.put("12", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa");
        descriptions.put("13", "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)");
        descriptions.put("24", "Giao dịch không thành công do: Khách hàng hủy giao dịch");
        descriptions.put("51",
                "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch");
        descriptions.put("65",
                "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày");
        descriptions.put("75", "Ngân hàng thanh toán đang bảo trì");
        descriptions.put("79", "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định");
        descriptions.put("99", "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)");

        return descriptions.getOrDefault(responseCode, "Unknown response code: " + responseCode);
    }
}
