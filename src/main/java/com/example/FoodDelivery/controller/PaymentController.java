package com.example.FoodDelivery.controller;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.service.OrderService;
import com.example.FoodDelivery.service.PaymentService;
import com.example.FoodDelivery.service.VNPayService;
import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.service.UserService;
import com.example.FoodDelivery.service.WalletTransactionService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final OrderService orderService;
    private final UserService userService;
    private final WalletTransactionService walletTransactionService;

    @Value("${frontend.home-url:https://eatzy-customer.hoanduong.net/home}")
    private String frontendHomeUrl;

    public PaymentController(
            PaymentService paymentService,
            VNPayService vnPayService,
            OrderService orderService,
            UserService userService,
            WalletTransactionService walletTransactionService) {
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
        this.orderService = orderService;
        this.userService = userService;
        this.walletTransactionService = walletTransactionService;
    }

    /**
     * Process wallet payment for an order
     */
    @PostMapping("/wallet")
    @ApiMessage("Process wallet payment")
    public ResponseEntity<Map<String, Object>> processWalletPayment(@RequestBody Map<String, Long> body)
            throws IdInvalidException {
        Long orderId = body.get("orderId");
        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        Map<String, Object> result = paymentService.processWalletPayment(order);

        // Update order payment status if successful
        if ((Boolean) result.get("success")) {
            order.setPaymentStatus("PAID");
            orderService.updateOrder(order);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Create VNPAY payment URL
     */
    @PostMapping("/vnpay/create")
    @ApiMessage("Create VNPAY payment URL")
    public ResponseEntity<Map<String, Object>> createVNPayPayment(
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) throws IdInvalidException, UnsupportedEncodingException {
        Long orderId = body.get("orderId");
        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get client IP address
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        // Get base URL from request
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String baseUrl = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            baseUrl += ":" + serverPort;
        }

        String paymentUrl = vnPayService.createPaymentUrl(order, ipAddress, baseUrl);

        Map<String, Object> result = Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "orderId", orderId);

        return ResponseEntity.ok(result);
    }

    /**
     * Create VNPAY payment URL for wallet top-up
     */
    @PostMapping("/vnpay/wallet/create")
    @ApiMessage("Create VNPAY wallet top-up URL")
    public ResponseEntity<Map<String, Object>> createVNPayWalletTopUp(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) throws IdInvalidException, UnsupportedEncodingException {

        if (!body.containsKey("amount")) {
            throw new IdInvalidException("Amount is required");
        }

        // Get current logged-in user
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not logged in"));

        com.example.FoodDelivery.domain.User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("User not found");
        }

        Long userId = user.getId();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        // Minimum top-up amount: 10,000 VND
        if (amount.compareTo(new BigDecimal("10000")) < 0) {
            throw new IdInvalidException("Amount must be at least 10,000 VND");
        }

        // Get client IP address
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        // Get base URL from request
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String baseUrl = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            baseUrl += ":" + serverPort;
        }

        String paymentUrl = vnPayService.createWalletTopUpUrl(userId, amount, ipAddress, baseUrl);

        Map<String, Object> result = Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "userId", userId,
                "amount", amount);

        return ResponseEntity.ok(result);
    }

    /**
     * VNPAY wallet top-up callback handler
     */
    @GetMapping("/vnpay/wallet-callback")
    @ApiMessage("VNPAY wallet top-up callback")
    public ResponseEntity<Map<String, Object>> vnpayWalletCallback(@RequestParam Map<String, String> params)
            throws IdInvalidException {
        Map<String, Object> result = vnPayService.processWalletTopUpCallback(params);
        return ResponseEntity.ok(result);
    }

    /**
     * VNPAY callback handler - redirects to frontend after payment
     */
    @GetMapping("/vnpay/callback")
    @ApiMessage("VNPAY payment callback")
    public void vnpayCallback(@RequestParam Map<String, String> params,
            HttpServletResponse response) throws IdInvalidException, java.io.IOException {
        Map<String, Object> result = vnPayService.processCallback(params);

        // Update order payment status
        Long orderId = (Long) result.get("orderId");
        if (orderId != null) {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                if ((Boolean) result.get("success")) {
                    order.setPaymentStatus("PAID");
                } else {
                    order.setPaymentStatus("FAILED");
                }
                orderService.updateOrder(order);
            }
        }

        // Redirect to frontend home page with payment result
        boolean success = (Boolean) result.get("success");
        String redirectUrl = frontendHomeUrl + "?payment=" + (success ? "success" : "failed");
        if (orderId != null) {
            redirectUrl += "&orderId=" + orderId;
        }
        response.sendRedirect(redirectUrl);
    }

    /**
     * Validate COD payment for an order
     */
    @PostMapping("/cod/validate")
    @ApiMessage("Validate COD payment")
    public ResponseEntity<Map<String, Object>> validateCODPayment(@RequestBody Map<String, Long> body)
            throws IdInvalidException {
        Long orderId = body.get("orderId");
        Long driverId = body.get("driverId");

        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        Map<String, Object> result = paymentService.validateCODPayment(order, driverId);
        return ResponseEntity.ok(result);
    }

    /**
     * Check if COD is available for given amount
     */
    @GetMapping("/cod/available")
    @ApiMessage("Check COD availability")
    public ResponseEntity<Map<String, Object>> checkCODAvailability(@RequestParam BigDecimal amount) {
        boolean available = paymentService.isCODAvailable(amount);
        Map<String, Object> result = Map.of(
                "available", available,
                "amount", amount);
        return ResponseEntity.ok(result);
    }

    /**
     * Process COD payment when order is delivered
     */
    @PostMapping("/cod/process")
    @ApiMessage("Process COD payment on delivery")
    public ResponseEntity<Map<String, Object>> processCODPayment(@RequestBody Map<String, Long> body)
            throws IdInvalidException {
        Long orderId = body.get("orderId");
        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Check if order is delivered
        if (!"DELIVERED".equals(order.getOrderStatus())) {
            throw new IdInvalidException("Order must be delivered before processing COD payment");
        }

        Map<String, Object> result = paymentService.processCODPaymentOnDelivery(order);

        // Update order payment status
        if ((Boolean) result.get("success")) {
            order.setPaymentStatus("PAID");
            orderService.updateOrder(order);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Calculate commissions for an order
     */
    @GetMapping("/commission/{orderId}")
    @ApiMessage("Calculate order commissions")
    public ResponseEntity<Map<String, BigDecimal>> calculateCommissions(@PathVariable Long orderId)
            throws IdInvalidException {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        Map<String, BigDecimal> commissions = paymentService.calculateCommissions(order);
        return ResponseEntity.ok(commissions);
    }

    /**
     * Process wallet withdrawal
     */
    @PostMapping("/withdraw")
    @ApiMessage("Process wallet withdrawal")
    public ResponseEntity<Map<String, Object>> withdraw(
            @RequestBody Map<String, Object> body) throws IdInvalidException {

        if (!body.containsKey("amount")) {
            throw new IdInvalidException("Amount is required");
        }

        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String description = (String) body.get("description");

        // Get current logged-in user
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not logged in"));

        com.example.FoodDelivery.domain.User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("User not found");
        }

        // Get user's wallet
        // We reuse the logic but need to get the walletId
        com.example.FoodDelivery.domain.Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found for current user");
        }

        com.example.FoodDelivery.domain.res.walletTransaction.resWalletTransactionDTO transaction = walletTransactionService
                .withdrawFromWallet(wallet.getId(), amount, description);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Withdrawal successful",
                "transaction", transaction));
    }
}
