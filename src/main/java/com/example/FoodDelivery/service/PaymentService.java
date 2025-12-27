package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.DriverProfile;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.SystemConfiguration;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Wallet;
import com.example.FoodDelivery.domain.WalletTransaction;
import com.example.FoodDelivery.domain.res.driverProfile.ResDriverProfileDTO;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;
    private final SystemConfigurationService systemConfigurationService;
    private final UserService userService;
    private final DriverProfileService driverProfileService;

    public PaymentService(
            WalletService walletService,
            @Lazy WalletTransactionService walletTransactionService,
            SystemConfigurationService systemConfigurationService,
            UserService userService,
            DriverProfileService driverProfileService) {
        this.walletService = walletService;
        this.walletTransactionService = walletTransactionService;
        this.systemConfigurationService = systemConfigurationService;
        this.userService = userService;
        this.driverProfileService = driverProfileService;
    }

    /**
     * Process wallet payment
     * Check if customer has sufficient balance, deduct from customer wallet
     * 
     * @param order
     * @return payment result
     * @throws IdInvalidException
     */
    @Transactional
    public Map<String, Object> processWalletPayment(Order order) throws IdInvalidException {
        Map<String, Object> result = new HashMap<>();

        if (order.getCustomer() == null) {
            throw new IdInvalidException("Customer is required for wallet payment");
        }

        // Get customer wallet
        Wallet customerWallet = walletService.getWalletByUserId(order.getCustomer().getId());
        if (customerWallet == null) {
            throw new IdInvalidException("Customer wallet not found");
        }

        // Check sufficient balance
        BigDecimal totalAmount = order.getTotalAmount();
        if (customerWallet.getBalance().compareTo(totalAmount) < 0) {
            result.put("success", false);
            result.put("message", "Insufficient wallet balance");
            result.put("currentBalance", customerWallet.getBalance());
            result.put("requiredAmount", totalAmount);
            return result;
        }

        // Create wallet transaction for customer
        WalletTransaction customerTransaction = WalletTransaction.builder()
                .wallet(customerWallet)
                .transactionType("PAYMENT")
                .amount(totalAmount.negate()) // negative for deduction
                .balanceAfter(customerWallet.getBalance().add(totalAmount.negate()))
                .description("Payment for order #" + order.getId())
                .order(order)
                .status("SUCCESS")
                .transactionDate(Instant.now())
                .build();
        walletTransactionService.createWalletTransaction(customerTransaction);

        // Add to admin wallet
        User admin = getAdminUser();
        if (admin != null) {
            Wallet adminWallet = walletService.getWalletByUserId(admin.getId());
            if (adminWallet != null) {
                // Create wallet transaction for admin
                WalletTransaction adminTransaction = WalletTransaction.builder()
                        .wallet(adminWallet)
                        .transactionType("PAYMENT_RECEIVED")
                        .amount(totalAmount)
                        .balanceAfter(adminWallet.getBalance().add(totalAmount))
                        .description("Payment received from order #" + order.getId())
                        .order(order)
                        .status("SUCCESS")
                        .transactionDate(Instant.now())
                        .build();
                walletTransactionService.createWalletTransaction(adminTransaction);
            }
        }

        result.put("success", true);
        result.put("message", "Payment successful");
        result.put("transactionAmount", totalAmount);
        result.put("remainingBalance", customerWallet.getBalance());

        return result;
    }

    /**
     * Process refund payment
     * Deduct from admin wallet and add to customer wallet
     * 
     * @param order
     * @return refund result
     * @throws IdInvalidException
     */
    @Transactional
    public Map<String, Object> processRefund(Order order) throws IdInvalidException {
        Map<String, Object> result = new HashMap<>();

        if (order.getCustomer() == null) {
            throw new IdInvalidException("Customer is required for refund");
        }

        BigDecimal refundAmount = order.getTotalAmount();

        // Get admin wallet
        User admin = getAdminUser();
        if (admin == null) {
            throw new IdInvalidException("Admin user not found");
        }

        Wallet adminWallet = walletService.getWalletByUserId(admin.getId());
        if (adminWallet == null) {
            throw new IdInvalidException("Admin wallet not found");
        }

        // Check sufficient balance in admin wallet
        if (adminWallet.getBalance().compareTo(refundAmount) < 0) {
            result.put("success", false);
            result.put("message", "Insufficient admin wallet balance for refund");
            result.put("currentBalance", adminWallet.getBalance());
            result.put("requiredAmount", refundAmount);
            return result;
        }

        // Get customer wallet
        Wallet customerWallet = walletService.getWalletByUserId(order.getCustomer().getId());
        if (customerWallet == null) {
            throw new IdInvalidException("Customer wallet not found");
        }

        // Deduct from admin wallet
        WalletTransaction adminTransaction = WalletTransaction.builder()
                .wallet(adminWallet)
                .transactionType("REFUND")
                .amount(refundAmount.negate()) // negative for deduction
                .balanceAfter(adminWallet.getBalance().add(refundAmount.negate()))
                .description("Refund for order #" + order.getId())
                .order(order)
                .status("SUCCESS")
                .transactionDate(Instant.now())
                .build();
        walletTransactionService.createWalletTransaction(adminTransaction);

        // Add to customer wallet
        WalletTransaction customerTransaction = WalletTransaction.builder()
                .wallet(customerWallet)
                .transactionType("REFUND")
                .amount(refundAmount)
                .balanceAfter(customerWallet.getBalance().add(refundAmount))
                .description("Refund for order #" + order.getId())
                .order(order)
                .status("SUCCESS")
                .transactionDate(Instant.now())
                .build();
        walletTransactionService.createWalletTransaction(customerTransaction);

        result.put("success", true);
        result.put("message", "Refund successful");
        result.put("refundAmount", refundAmount);
        result.put("newBalance", customerWallet.getBalance());

        return result;
    }

    /**
     * Validate COD payment
     * Check if order amount is within driver's COD limit
     * 
     * @param order
     * @param driverId
     * @return validation result
     * @throws IdInvalidException
     */
    public Map<String, Object> validateCODPayment(Order order, Long driverId) throws IdInvalidException {
        Map<String, Object> result = new HashMap<>();

        if (driverId != null) {
            // Get driver profile
            ResDriverProfileDTO driverProfile = driverProfileService.getDriverProfileByUserId(driverId);
            if (driverProfile == null) {
                result.put("valid", false);
                result.put("message", "Driver profile not found");
                return result;
            }

            // Check COD limit
            BigDecimal codLimit = driverProfile.getCodLimit();
            if (codLimit == null) {
                codLimit = getDefaultCODLimit();
            }

            BigDecimal totalAmount = order.getTotalAmount();
            if (totalAmount.compareTo(codLimit) > 0) {
                result.put("valid", false);
                result.put("message", "Order amount exceeds COD limit");
                result.put("orderAmount", totalAmount);
                result.put("codLimit", codLimit);
                return result;
            }
        } else {
            // No driver assigned yet, check with default COD limit
            BigDecimal defaultCODLimit = getDefaultCODLimit();
            BigDecimal totalAmount = order.getTotalAmount();
            if (totalAmount.compareTo(defaultCODLimit) > 0) {
                result.put("valid", false);
                result.put("message", "Order amount exceeds default COD limit");
                result.put("orderAmount", totalAmount);
                result.put("codLimit", defaultCODLimit);
                return result;
            }
        }

        result.put("valid", true);
        result.put("message", "COD payment is valid");
        return result;
    }

    /**
     * Check if COD is available for order amount
     * 
     * @param orderAmount
     * @return true if COD is available
     */
    public boolean isCODAvailable(BigDecimal orderAmount) {
        BigDecimal defaultCODLimit = getDefaultCODLimit();
        return orderAmount.compareTo(defaultCODLimit) <= 0;
    }

    /**
     * Process COD payment when order is delivered
     * Add money to admin wallet after successful delivery
     * 
     * @param order
     * @return payment result
     * @throws IdInvalidException
     */
    @Transactional
    public Map<String, Object> processCODPaymentOnDelivery(Order order) throws IdInvalidException {
        Map<String, Object> result = new HashMap<>();

        // Get driver wallet
        Wallet driverWallet = walletService.getWalletByUserId(order.getDriver().getId());
        if (driverWallet == null) {
            throw new IdInvalidException("Driver wallet not found");
        }

        // Check sufficient balance
        BigDecimal totalAmount = order.getTotalAmount();

        // Deduct from driver wallet
        walletService.subtractBalance(driverWallet.getId(), totalAmount);

        // Create wallet transaction for driver
        WalletTransaction driverTransaction = WalletTransaction.builder()
                .wallet(driverWallet)
                .transactionType("PAYMENT")
                .amount(totalAmount.negate()) // negative for deduction
                .balanceAfter(driverWallet.getBalance())
                .description("Payment for order #" + order.getId())
                .order(order)
                .transactionDate(Instant.now())
                .build();
        walletTransactionService.createWalletTransaction(driverTransaction);

        // Add to admin wallet
        User admin = getAdminUser();
        if (admin == null) {
            throw new IdInvalidException("Admin user not found");
        }

        Wallet adminWallet = walletService.getWalletByUserId(admin.getId());
        if (adminWallet == null) {
            throw new IdInvalidException("Admin wallet not found");
        }

        walletService.addBalance(adminWallet.getId(), totalAmount);

        // Create wallet transaction for admin
        WalletTransaction adminTransaction = WalletTransaction.builder()
                .wallet(adminWallet)
                .transactionType("COD_RECEIVED")
                .amount(totalAmount)
                .balanceAfter(adminWallet.getBalance())
                .description("COD payment received from order #" + order.getId())
                .order(order)
                .transactionDate(Instant.now())
                .build();
        walletTransactionService.createWalletTransaction(adminTransaction);

        result.put("success", true);
        result.put("message", "COD payment recorded successfully");
        result.put("amount", totalAmount);

        return result;
    }

    /**
     * Get admin user (role = ADMIN)
     * 
     * @return admin user
     */
    private User getAdminUser() {
        try {
            // Get admin user by role name "ADMIN"
            return userService.getUserByRoleName("ADMIN");
        } catch (Exception e) {
            log.error("Error getting admin user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get default COD limit from system configuration
     * 
     * @return default COD limit
     */
    private BigDecimal getDefaultCODLimit() {
        try {
            SystemConfiguration config = systemConfigurationService.getSystemConfigurationByKey("default_cod_limit");
            if (config != null && config.getConfigValue() != null) {
                return new BigDecimal(config.getConfigValue());
            }
        } catch (Exception e) {
            log.error("Error getting default COD limit: {}", e.getMessage());
        }
        // Default to 5,000,000 VND if not configured
        return new BigDecimal("5000000");
    }

    /**
     * Get driver commission percentage from system configuration
     * 
     * @return driver commission percentage
     */
    public BigDecimal getDriverCommission() {
        try {
            SystemConfiguration config = systemConfigurationService
                    .getSystemConfigurationByKey("driver_commission_percentage");
            if (config != null && config.getConfigValue() != null) {
                return new BigDecimal(config.getConfigValue());
            }
        } catch (Exception e) {
            log.error("Error getting driver commission: {}", e.getMessage());
        }
        // Default to 15%
        return new BigDecimal("15");
    }

    /**
     * Get restaurant commission percentage from system configuration
     * 
     * @return restaurant commission percentage
     */
    public BigDecimal getRestaurantCommission() {
        try {
            SystemConfiguration config = systemConfigurationService
                    .getSystemConfigurationByKey("restaurant_commission_percentage");
            if (config != null && config.getConfigValue() != null) {
                return new BigDecimal(config.getConfigValue());
            }
        } catch (Exception e) {
            log.error("Error getting restaurant commission: {}", e.getMessage());
        }
        // Default to 20%
        return new BigDecimal("20");
    }

    /**
     * Calculate commission amounts for driver and restaurant
     * 
     * @param order
     * @return map with driver and restaurant commission amounts
     */
    public Map<String, BigDecimal> calculateCommissions(Order order) {
        Map<String, BigDecimal> commissions = new HashMap<>();

        BigDecimal driverCommissionPercent = getDriverCommission();
        BigDecimal restaurantCommissionPercent = getRestaurantCommission();

        // Calculate driver commission from delivery fee
        BigDecimal driverCommission = BigDecimal.ZERO;
        if (order.getDeliveryFee() != null) {
            driverCommission = order.getDeliveryFee()
                    .multiply(driverCommissionPercent)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        }

        // Calculate restaurant commission from subtotal
        BigDecimal restaurantCommission = BigDecimal.ZERO;
        if (order.getSubtotal() != null) {
            restaurantCommission = order.getSubtotal()
                    .multiply(restaurantCommissionPercent)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        }

        commissions.put("driverCommission", driverCommission);
        commissions.put("restaurantCommission", restaurantCommission);
        commissions.put("driverEarnings", order.getDeliveryFee() != null
                ? order.getDeliveryFee().subtract(driverCommission)
                : BigDecimal.ZERO);
        commissions.put("restaurantEarnings", order.getSubtotal() != null
                ? order.getSubtotal().subtract(restaurantCommission)
                : BigDecimal.ZERO);

        return commissions;
    }
}
