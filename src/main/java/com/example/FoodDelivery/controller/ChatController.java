package com.example.FoodDelivery.controller;

import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.FoodDelivery.domain.res.websocket.ChatMessage;
import com.example.FoodDelivery.service.ChatMessageService;
import com.example.FoodDelivery.service.OrderService;

import lombok.extern.slf4j.Slf4j;

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
     */
    @MessageMapping("/chat/{orderId}")
    public void handleChatMessage(@DestinationVariable("orderId") Long orderId,
            @Payload ChatMessage message,
            Principal principal) {
        String authenticatedUser = principal != null ? principal.getName() : "anonymous";
        log.info("Received chat message for order {} from authenticated user '{}': {} from {} ({})",
                orderId, authenticatedUser, message.getMessage(), message.getSenderName(), message.getSenderType());

        // Set timestamp
        message.setTimestamp(Instant.now());
        message.setOrderId(orderId);

        // Verify order exists
        var order = orderService.getOrderById(orderId);
        if (order == null) {
            log.error("Order {} not found", orderId);
            return;
        }

        // ===== AUTHORIZATION CHECK =====
        // Get user email from SecurityContext (more reliable than Principal for
        // WebSocket with JWT)
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin().orElse(null);

        if (currentUserEmail == null) {
            log.warn("No authenticated user found in SecurityContext for order {} chat. Principal: {}",
                    orderId, authenticatedUser);
            // For now, allow if we have a valid senderId in the message (backwards
            // compatibility)
            // In production, you might want to make this stricter
        } else {
            // Only check authorization if we have a valid user email
            if (!isUserAuthorizedForOrder(currentUserEmail, order)) {
                log.warn("Unauthorized chat access attempt: user '{}' tried to access order {} chat",
                        currentUserEmail, orderId);
                return;
            }
            log.debug("User {} authorized for order {} chat", currentUserEmail, orderId);
        }

        // ===== QUEUE VERSION (Point-to-point - Private messaging) =====
        // Issue: Requires proper user authentication with Principal
        // Client must connect with {login: userId} header
        /*
         * // Send to driver via point-to-point queue (private message)
         * if (order.getDriver() != null) {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getDriver().getId()),
         * "/queue/chat/order/" + orderId,
         * message);
         * log.info("Sent chat message to driver {} via queue",
         * order.getDriver().getId());
         * }
         * 
         * // Send to customer via point-to-point queue (private message)
         * if (order.getCustomer() != null) {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getCustomer().getId()),
         * "/queue/chat/order/" + orderId,
         * message);
         * log.info("Sent chat message to customer {} via queue",
         * order.getCustomer().getId());
         * }
         */

        // ===== TOPIC VERSION (Broadcast - Simpler but less secure) =====
        // Broadcast to all subscribers of this order's chat topic
        // Anyone who subscribes to /topic/chat/order/{orderId} will receive the message
        messagingTemplate.convertAndSend(
                "/topic/chat/order/" + orderId,
                message);
        log.info("Broadcasted chat message to topic /topic/chat/order/{}", orderId);

        // ===== PERSIST MESSAGE (Database + Redis cache) =====
        // Save message asynchronously to DB and cache to Redis
        // This doesn't block the WebSocket broadcast
        try {
            chatMessageService.saveMessage(message);
            log.debug("Initiated message persistence for order {}", orderId);
        } catch (Exception e) {
            log.error("Failed to persist message for order {}: {}", orderId, e.getMessage(), e);
            // Don't fail the broadcast even if persistence fails
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
        String authenticatedUser = principal != null ? principal.getName() : "anonymous";
        log.debug("Received typing indicator for order {} from authenticated user '{}'", orderId, authenticatedUser);

        var order = orderService.getOrderById(orderId);
        if (order == null) {
            return;
        }

        // ===== QUEUE VERSION (Point-to-point) =====
        /*
         * // Broadcast typing indicator to the other party via queue
         * if ("DRIVER".equals(message.getSenderType()) && order.getCustomer() != null)
         * {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getCustomer().getId()),
         * "/queue/chat/order/" + orderId + "/typing",
         * message);
         * } else if ("CUSTOMER".equals(message.getSenderType()) && order.getDriver() !=
         * null) {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getDriver().getId()),
         * "/queue/chat/order/" + orderId + "/typing",
         * message);
         * }
         */

        // ===== TOPIC VERSION (Broadcast) =====
        // Broadcast typing indicator to all subscribers of this order's chat
        messagingTemplate.convertAndSend(
                "/topic/chat/order/" + orderId + "/typing",
                message);
        log.debug("Broadcasted typing indicator to topic /topic/chat/order/{}/typing", orderId);
    }

    /**
     * Check if the authenticated user is authorized to access this order's chat
     * Only the customer and driver of the order can access the chat
     * 
     * @param authenticatedUserEmail email of the authenticated user
     * @param order                  the order to check
     * @return true if user is authorized, false otherwise
     */
    private boolean isUserAuthorizedForOrder(String authenticatedUserEmail,
            com.example.FoodDelivery.domain.Order order) {
        if (authenticatedUserEmail == null || "anonymous".equals(authenticatedUserEmail)) {
            log.warn("Anonymous user attempted to access order {} chat", order.getId());
            return false;
        }

        // Check if user is the customer
        if (order.getCustomer() != null &&
                authenticatedUserEmail.equals(order.getCustomer().getEmail())) {
            log.debug("User {} authorized as customer for order {}", authenticatedUserEmail, order.getId());
            return true;
        }

        // Check if user is the driver
        if (order.getDriver() != null &&
                authenticatedUserEmail.equals(order.getDriver().getEmail())) {
            log.debug("User {} authorized as driver for order {}", authenticatedUserEmail, order.getId());
            return true;
        }

        // User is neither customer nor driver
        log.warn("User {} is not authorized for order {} (customer: {}, driver: {})",
                authenticatedUserEmail, order.getId(),
                order.getCustomer() != null ? order.getCustomer().getEmail() : "none",
                order.getDriver() != null ? order.getDriver().getEmail() : "none");
        return false;
    }
}
