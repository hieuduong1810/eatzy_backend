package com.example.FoodDelivery.controller;

import java.time.Instant;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.websocket.DriverLocationUpdate;
import com.example.FoodDelivery.service.RedisGeoService;
import com.example.FoodDelivery.service.UserService;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DriverLocationController {

    private final RedisGeoService redisGeoService;
    private final UserService userService;
    private final com.example.FoodDelivery.repository.OrderRepository orderRepository;
    private final com.example.FoodDelivery.service.WebSocketService webSocketService;

    public DriverLocationController(
            RedisGeoService redisGeoService,
            UserService userService,
            com.example.FoodDelivery.repository.OrderRepository orderRepository,
            com.example.FoodDelivery.service.WebSocketService webSocketService) {
        this.redisGeoService = redisGeoService;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.webSocketService = webSocketService;
    }

    /**
     * Handle driver location updates
     * Driver sends location via: /app/driver/location
     * Location is stored in Redis GEO for real-time tracking
     */
    @MessageMapping("/driver/location")
    public void updateDriverLocation(@Payload DriverLocationUpdate locationUpdate) {
        // Get authenticated driver
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElse(null);

        if (currentUserEmail == null) {
            log.error("User not authenticated");
            return;
        }

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);

        if (driver == null) {
            log.error("Driver not found: {}", currentUserEmail);
            return;
        }

        // Set timestamp
        locationUpdate.setTimestamp(Instant.now());

        // Save location to Redis GEO
        redisGeoService.updateDriverLocation(
                driver.getId(),
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude());

        log.debug("📍 Driver {} location updated in Redis GEO: lat={}, lng={}",
                driver.getId(),
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude());

        // Broadcast to customer if driver has active order
        try {
            java.util.List<String> activeStatuses = java.util.List.of("DRIVER_ASSIGNED", "On the way to restaurant",
                    "Arrived at restaurant", "Picked up order", "DELIVERING", "Arrived at delivery location");
            com.example.FoodDelivery.domain.Order activeOrder = orderRepository
                    .findFirstByDriverIdAndOrderStatusIn(driver.getId(), activeStatuses);

            if (activeOrder != null && activeOrder.getCustomer() != null) {
                webSocketService.sendDriverLocationToCustomer(activeOrder.getCustomer().getEmail(), locationUpdate);
            }
        } catch (Exception e) {
            log.error("Failed to broadcast driver location to customer", e);
        }
    }
}
