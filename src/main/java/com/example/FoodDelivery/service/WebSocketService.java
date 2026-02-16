package com.example.FoodDelivery.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.res.order.ResOrderDTO;
import com.example.FoodDelivery.domain.res.websocket.DriverLocationUpdate;
import com.example.FoodDelivery.domain.res.websocket.OrderNotification;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket service for sending real-time notifications to users.
 * Uses user-specific destinations for security - messages are routed by user
 * email.
 */
@Service
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ==================== USER-SPECIFIC METHODS (SECURE) ====================

    /**
     * Send notification to a specific user by email.
     * The user must be subscribed to /user/queue/{destination}
     * 
     * @param email       User's email (used as principal name)
     * @param destination Destination without /user prefix (e.g., "/queue/orders")
     * @param payload     The message payload
     */
    public void sendToUser(String email, String destination, Object payload) {
        if (email == null || email.isEmpty()) {
            log.warn("Cannot send message - email is null or empty");
            return;
        }
        messagingTemplate.convertAndSendToUser(email, destination, payload);
        log.info("Sent message to user {} at /user/{}{}", email, email, destination);
    }

    /**
     * Notify restaurant owner about new order.
     * Restaurant must subscribe to: /user/queue/orders
     */
    public void notifyRestaurantNewOrder(String ownerEmail, ResOrderDTO order) {
        OrderNotification notification = new OrderNotification(
                "NEW_ORDER",
                order.getId(),
                "Có đơn hàng mới từ khách hàng",
                order);

        sendToUser(ownerEmail, "/queue/orders", notification);
        log.info("Sent NEW_ORDER notification to restaurant owner: {}", ownerEmail);
    }

    /**
     * Notify driver about assigned order.
     * Driver must subscribe to: /user/queue/orders
     */
    public void notifyDriverOrderAssigned(String driverEmail, ResOrderDTO order) {
        OrderNotification notification = new OrderNotification(
                "ORDER_ASSIGNED",
                order.getId(),
                "Có đơn hàng mới được giao cho bạn",
                order);

        sendToUser(driverEmail, "/queue/orders", notification);
        log.info("Sent ORDER_ASSIGNED notification to driver: {}", driverEmail);
    }

    /**
     * Notify customer about order update.
     * Customer must subscribe to: /user/queue/orders
     */
    public void notifyCustomerOrderUpdate(String customerEmail, ResOrderDTO order, String message) {
        OrderNotification notification = new OrderNotification(
                "ORDER_UPDATE",
                order.getId(),
                message,
                order);

        sendToUser(customerEmail, "/queue/orders", notification);
        log.info("Sent ORDER_UPDATE notification to customer: {}", customerEmail);
    }

    /**
     * Broadcast order status change to all relevant parties (customer, restaurant,
     * driver).
     * Each party must subscribe to: /user/queue/orders
     * 
     * @param order           The order DTO
     * @param customerEmail   Customer's email
     * @param restaurantEmail Restaurant owner's email
     * @param driverEmail     Driver's email (can be null if not assigned)
     */
    public void broadcastOrderStatusChange(ResOrderDTO order, String customerEmail,
            String restaurantEmail, String driverEmail) {
        OrderNotification notification = new OrderNotification(
                "ORDER_STATUS_CHANGED",
                order.getId(),
                "Đơn hàng đã thay đổi trạng thái: " + order.getOrderStatus(),
                order);

        // Notify customer
        if (customerEmail != null && !customerEmail.isEmpty()) {
            sendToUser(customerEmail, "/queue/orders", notification);
        }

        // Notify restaurant owner
        if (restaurantEmail != null && !restaurantEmail.isEmpty()) {
            sendToUser(restaurantEmail, "/queue/orders", notification);
        }

        // Notify driver (if assigned)
        if (driverEmail != null && !driverEmail.isEmpty()) {
            sendToUser(driverEmail, "/queue/orders", notification);
        }

        log.info("Broadcasted ORDER_STATUS_CHANGED for order {} to customer: {}, restaurant: {}, driver: {}",
                order.getId(), customerEmail, restaurantEmail, driverEmail);
    }

    /**
     * Send driver location update to customer.
     * Customer must subscribe to: /user/queue/driver-location
     */
    public void sendDriverLocationToCustomer(String customerEmail, DriverLocationUpdate locationUpdate) {
        sendToUser(customerEmail, "/queue/driver-location", locationUpdate);
        log.debug("Sent driver location to customer {}: {}, {}",
                customerEmail,
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude());
    }
