package com.example.FoodDelivery.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodDelivery.domain.res.websocket.ChatMessage;
import com.example.FoodDelivery.service.ChatMessageService;
import com.example.FoodDelivery.service.OrderService;
import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.annotation.ApiMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * REST API controller for chat message history
 * Provides endpoints to retrieve cached/stored chat messages
 */
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatRestController {

    private final ChatMessageService chatMessageService;
    private final OrderService orderService;

    public ChatRestController(ChatMessageService chatMessageService, OrderService orderService) {
        this.chatMessageService = chatMessageService;
        this.orderService = orderService;
    }

    /**
     * Get chat message history for an order
     * Returns messages from Redis cache or Database fallback
     * 
     * @param orderId order ID
     * @param page    page number (default: 0)
     * @param size    page size (default: 50)
     * @return list of chat messages
     */
    @GetMapping("/order/{orderId}")
    @ApiMessage("Get chat history successfully")
    public ResponseEntity<List<ChatMessage>> getOrderChatHistory(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            log.info("Fetching chat history for order {}, page={}, size={}", orderId, page, size);

            // Authorization check: only customer or driver of this order can view chat
            var order = orderService.getOrderById(orderId);
            if (order == null) {
                log.error("Order {} not found", orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String currentUserEmail = SecurityUtil.getCurrentUserLogin().orElse(null);
            if (!isUserAuthorizedForOrder(currentUserEmail, order)) {
                log.warn("Unauthorized access attempt to chat history: user '{}' for order {}",
                        currentUserEmail, orderId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<ChatMessage> messages = chatMessageService.getMessageHistory(orderId, page, size);

            log.debug("Retrieved {} messages for order {}", messages.size(), orderId);

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Failed to get chat history for order {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get message count for an order
     * 
     * @param orderId order ID
     * @return count of messages
     */
    @GetMapping("/order/{orderId}/count")
    @ApiMessage("Get message count successfully")
    public ResponseEntity<Long> getMessageCount(@PathVariable Long orderId) {
        try {
            log.info("Fetching message count for order {}", orderId);

            // Authorization check
            var order = orderService.getOrderById(orderId);
            if (order == null) {
                log.error("Order {} not found", orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String currentUserEmail = SecurityUtil.getCurrentUserLogin().orElse(null);
            if (!isUserAuthorizedForOrder(currentUserEmail, order)) {
                log.warn("Unauthorized access attempt to message count: user '{}' for order {}",
                        currentUserEmail, orderId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Long count = chatMessageService.getMessageCount(orderId);

            log.debug("Order {} has {} messages", orderId, count);

            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to get message count for order {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
