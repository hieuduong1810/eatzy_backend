package com.example.FoodDelivery.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.SystemConfiguration;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderCleanupService {
    private final OrderRepository orderRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final OrderService orderService;

    public OrderCleanupService(OrderRepository orderRepository,
            SystemConfigurationService systemConfigurationService,
            OrderService orderService) {
        this.orderRepository = orderRepository;
        this.systemConfigurationService = systemConfigurationService;
        this.orderService = orderService;
    }

    /**
     * Automatically delete expired VNPAY orders that were never paid
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void cleanupExpiredVNPayOrders() {
        try {
            // Calculate cutoff time: 15 minutes ago
            Instant cutoffTime = Instant.now().minus(15, ChronoUnit.MINUTES);

            // Find all VNPAY orders that are UNPAID and older than 15 minutes
            List<Order> expiredOrders = orderRepository.findByPaymentMethodAndPaymentStatusAndCreatedAtBefore(
                    "VNPAY", "UNPAID", cutoffTime);

            if (!expiredOrders.isEmpty()) {
                log.info("Found {} expired VNPAY orders to cleanup", expiredOrders.size());

                for (Order order : expiredOrders) {
                    log.info("Deleting expired VNPAY order: orderId={}, createdAt={}, age={}minutes",
                            order.getId(),
                            order.getCreatedAt(),
                            ChronoUnit.MINUTES.between(order.getCreatedAt(), Instant.now()));

                    orderRepository.delete(order);
                }

                log.info("Successfully cleaned up {} expired VNPAY orders", expiredOrders.size());
            }
        } catch (Exception e) {
            log.error("Error during VNPAY order cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Automatically cancel PENDING and PREPARING orders that exceed timeout
     * - PENDING orders: cancelled with reason "Restaurant did not respond"
     * - PREPARING orders: cancelled with reason "No driver available"
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void autoCancelStaleOrders() {
        try {
            // Get timeout for PENDING orders (restaurant response timeout)
            int restaurantTimeoutMinutes = 15;
            try {
                SystemConfiguration config = systemConfigurationService
                        .getSystemConfigurationByKey("RESTAURANT_RESPONSE_TIMEOUT_MINUTES");
                if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                    restaurantTimeoutMinutes = Integer.parseInt(config.getConfigValue());
                }
            } catch (Exception e) {
                log.warn("Failed to get RESTAURANT_RESPONSE_TIMEOUT_MINUTES config, using default {} minutes",
                        restaurantTimeoutMinutes);
            }

            // Get timeout for PREPARING orders (driver assignment timeout)
            int driverTimeoutMinutes = 30;
            try {
                SystemConfiguration config = systemConfigurationService
                        .getSystemConfigurationByKey("DRIVER_ASSIGNMENT_TIMEOUT_MINUTES");
                if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                    driverTimeoutMinutes = Integer.parseInt(config.getConfigValue());
                }
            } catch (Exception e) {
                log.warn("Failed to get DRIVER_ASSIGNMENT_TIMEOUT_MINUTES config, using default {} minutes",
                        driverTimeoutMinutes);
            }

            // Process PENDING orders
            Instant restaurantCutoffTime = Instant.now().minus(restaurantTimeoutMinutes, ChronoUnit.MINUTES);
            List<Order> pendingOrders = orderRepository.findByOrderStatusAndCreatedAtBefore("PENDING",
                    restaurantCutoffTime);
            if (!pendingOrders.isEmpty()) {
                log.info("Found {} PENDING orders to auto-cancel (timeout: {} minutes)",
                        pendingOrders.size(), restaurantTimeoutMinutes);

                for (Order order : pendingOrders) {
                    try {
                        log.info("Auto-cancelling PENDING order: orderId={}, createdAt={}, age={}minutes",
                                order.getId(),
                                order.getCreatedAt(),
                                ChronoUnit.MINUTES.between(order.getCreatedAt(), Instant.now()));

                        orderService.cancelOrder(order.getId(), "Restaurant did not respond in time");
                    } catch (IdInvalidException e) {
                        log.error("Failed to cancel PENDING order {}: {}", order.getId(), e.getMessage());
                    }
                }

                log.info("Successfully processed {} PENDING orders for auto-cancellation", pendingOrders.size());
            }

            // Process PREPARING orders
            Instant driverCutoffTime = Instant.now().minus(driverTimeoutMinutes, ChronoUnit.MINUTES);
            List<Order> preparingOrders = orderRepository.findByOrderStatusAndPreparingAtBefore("PREPARING",
                    driverCutoffTime);
            if (!preparingOrders.isEmpty()) {
                log.info("Found {} PREPARING orders to auto-cancel (timeout: {} minutes)",
                        preparingOrders.size(), driverTimeoutMinutes);

                for (Order order : preparingOrders) {
                    try {
                        log.info("Auto-cancelling PREPARING order: orderId={}, preparingAt={}, age={}minutes",
                                order.getId(),
                                order.getPreparingAt(),
                                ChronoUnit.MINUTES.between(order.getPreparingAt(), Instant.now()));

                        orderService.cancelOrder(order.getId(), "No driver available");
                    } catch (IdInvalidException e) {
                        log.error("Failed to cancel PREPARING order {}: {}", order.getId(), e.getMessage());
                    }
                }

                log.info("Successfully processed {} PREPARING orders for auto-cancellation", preparingOrders.size());
            }
        } catch (Exception e) {
            log.error("Error during auto-cancel cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Automatically accept orders assigned to drivers who haven't responded within timeout
     * Runs every 10 seconds
     */
    @Scheduled(fixedRate = 10000) // 10 seconds = 10,000 milliseconds
    @Transactional
    public void autoAcceptUnrespondedOrders() {
        try {
            // Get timeout from system configuration (default 30 seconds)
            int acceptTimeoutSeconds = 30;
            try {
                SystemConfiguration config = systemConfigurationService
                        .getSystemConfigurationByKey("DRIVER_ACCEPT_TIMEOUT_SEC");
                if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                    acceptTimeoutSeconds = Integer.parseInt(config.getConfigValue());
                }
            } catch (Exception e) {
                log.warn("Failed to get DRIVER_ACCEPT_TIMEOUT_SEC config, using default {} seconds",
                        acceptTimeoutSeconds);
            }

            // Calculate cutoff time
            Instant cutoffTime = Instant.now().minus(acceptTimeoutSeconds, ChronoUnit.SECONDS);

            // Find orders with status PREPARING, driver assigned, but not accepted yet (assignedAt is set)
            List<Order> unrespondedOrders = orderRepository
                    .findByOrderStatusAndDriverIsNotNullAndAssignedAtBefore("PREPARING", cutoffTime);

            if (!unrespondedOrders.isEmpty()) {
                log.info("Found {} orders with unresponsive drivers (timeout: {} seconds)",
                        unrespondedOrders.size(), acceptTimeoutSeconds);

                for (Order order : unrespondedOrders) {
                    try {
                        log.info("Auto-accepting order: orderId={}, driverId={}, assignedAt={}, age={}seconds",
                                order.getId(),
                                order.getDriver().getId(),
                                order.getAssignedAt(),
                                ChronoUnit.SECONDS.between(order.getAssignedAt(), Instant.now()));

                        // Auto-accept the order using internal method
                        orderService.internalAcceptOrderByDriver(order.getId(), order.getDriver().getId());
                        
                        log.info("✅ Auto-accepted order {} for driver {} due to timeout", 
                                order.getId(), order.getDriver().getId());
                    } catch (IdInvalidException e) {
                        log.error("Failed to auto-accept order {}: {}", order.getId(), e.getMessage());
                    }
                }

                log.info("Successfully processed {} unresponsive driver orders", unrespondedOrders.size());
            }
        } catch (Exception e) {
            log.error("Error during auto-accept cleanup: {}", e.getMessage(), e);
        }
    }
}
