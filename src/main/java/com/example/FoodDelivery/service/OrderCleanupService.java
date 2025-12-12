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
}
