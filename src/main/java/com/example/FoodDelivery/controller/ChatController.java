package com.example.FoodDelivery.controller;

import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.res.websocket.ChatMessage;
import com.example.FoodDelivery.service.ChatMessageService;
import com.example.FoodDelivery.service.OrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling WebSocket chat messages.
 * Uses secure user-specific queues for private messaging between driver and
 * customer.
 */
@Controller
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderService orderService;
    private final ChatMessageService chatMessageService;

    public ChatController(SimpMessagingTemplate messagingTemplate,
            OrderService orderService,
            ChatMessageService chatMessageService) {
        this.messagingTemplate = messagingTemplate;
        this.orderService = orderService;
        this.chatMessageService = chatMessageService;
    }

    /**
     * Handle chat messages sent from client
     * Endpoint: /app/chat/{orderId}
     * 
     * Uses secure user-specific queues to send messages only to authorized parties
     * (driver and customer of the order).
     */
    @MessageMapping("/chat/{orderId}")
    public void handleChatMessage(@DestinationVariable("orderId") Long orderId,
            @Payload ChatMessage message,
            Principal principal) {
        String authenticatedUser = principal != null ? principal.getName() : null;
        log.info("Received chat message for order {} from user '{}': {}",
                orderId, authenticatedUser, message.getMessage());

        // Require authentication
        if (authenticatedUser == null) {
            log.warn("Unauthorized chat attempt - no authenticated user");
            return;
        }

        // Set timestamp
        message.setTimestamp(Instant.now());
        message.setOrderId(orderId);

        // Verify order exists
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            log.error("Order {} not found", orderId);
            return;
        }

        // Check authorization - only driver and customer of the order can chat
        if (!isUserAuthorizedForOrder(authenticatedUser, order)) {
            log.warn("Unauthorized chat access: user '{}' tried to access order {} chat",
                    authenticatedUser, orderId);
            return;
        }

        // Get emails for both parties
        String customerEmail = order.getCustomer() != null ? order.getCustomer().getEmail() : null;
        String driverEmail = order.getDriver() != null && order.getDriver().getDriverProfile() != null
                ? order.getDriver().getEmail()
                : null;

        // Send to customer via user-specific queue (if not the sender)
        if (customerEmail != null) {
            messagingTemplate.convertAndSendToUser(
                    customerEmail,
                    "/queue/chat/order/" + orderId,
                    message);
            log.debug("Sent chat message to customer: {}", customerEmail);
        }

        // Send to driver via user-specific queue (if assigned and not the sender)
        if (driverEmail != null) {
            messagingTemplate.convertAndSendToUser(
                    driverEmail,
                    "/queue/chat/order/" + orderId,
                    message);
            log.debug("Sent chat message to driver: {}", driverEmail);
        }

        log.info("Chat message delivered for order {} (customer: {}, driver: {})",
                orderId, customerEmail, driverEmail);

        // Persist message to database and cache
        try {
            chatMessageService.saveMessage(message);
            log.debug("Initiated message persistence for order {}", orderId);
        } catch (Exception e) {
            log.error("Failed to persist message for order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Handle typing indicator
     * Endpoint: /app/typing/{orderId}
     */
    @MessageMapping("/typing/{orderId}")
    public void handleTypingIndicator(@DestinationVariable("orderId") Long orderId,
            @Payload ChatMessage message,
            Principal principal) {
        String authenticatedUser = principal != null ? principal.getName() : null;

        if (authenticatedUser == null) {
            return;
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return;
        }

        if (!isUserAuthorizedForOrder(authenticatedUser, order)) {
            return;
        }

        // Get the OTHER party's email (not the sender)
        String targetEmail = null;

        if ("DRIVER".equals(message.getSenderType())) {
            // Driver is typing, notify customer
            targetEmail = order.getCustomer() != null ? order.getCustomer().getEmail() : null;
        } else if ("CUSTOMER".equals(message.getSenderType())) {
            // Customer is typing, notify driver
            targetEmail = order.getDriver() != null ? order.getDriver().getEmail() : null;
        }

        if (targetEmail != null) {
            messagingTemplate.convertAndSendToUser(
                    targetEmail,
                    "/queue/chat/order/" + orderId + "/typing",
                    message);
            log.debug("Sent typing indicator to {} for order {}", targetEmail, orderId);
        }
    }

    /**
     * Check if the authenticated user is authorized to access this order's chat
     * Only the customer and driver of the order can access the chat
     */
    private boolean isUserAuthorizedForOrder(String authenticatedUserEmail, Order order) {
        if (authenticatedUserEmail == null) {
            return false;
        }

        // Check if user is the customer
        if (order.getCustomer() != null &&
                authenticatedUserEmail.equals(order.getCustomer().getEmail())) {
            return true;
        }

        // Check if user is the driver
        if (order.getDriver() != null &&
                authenticatedUserEmail.equals(order.getDriver().getEmail())) {
            return true;
        }

        return false;
    }
}
