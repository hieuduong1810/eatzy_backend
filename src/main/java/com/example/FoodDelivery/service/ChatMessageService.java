package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.ChatMessage;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.repository.ChatMessageRepository;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Business logic service for chat messages
 * Orchestrates between Database (permanent) and Redis (cache)
 * 
 * Strategy:
 * - Write: Async to DB + Sync to Redis cache
 * - Read: Try Redis first → Fallback to DB if not cached
 */
@Service
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final RedisChatService redisChatService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ChatMessageService(
            ChatMessageRepository chatMessageRepository,
            RedisChatService redisChatService,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.redisChatService = redisChatService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a chat message to both Database (async) and Redis (sync)
     * 
     * @param websocketMessage the WebSocket message DTO
     */
    public void saveMessage(com.example.FoodDelivery.domain.res.websocket.ChatMessage websocketMessage) {
        try {
            // 1. Cache to Redis immediately (sync - fast)
            redisChatService.cacheMessage(websocketMessage);

            // 2. Save to Database asynchronously (doesn't block)
            saveToDatabase(websocketMessage);

            log.info("💬 Saved chat message: order={}, sender={}, type={}",
                    websocketMessage.getOrderId(),
                    websocketMessage.getSenderId(),
                    websocketMessage.getSenderType());
        } catch (Exception e) {
            log.error("Failed to save chat message: order={}, sender={}",
                    websocketMessage.getOrderId(),
                    websocketMessage.getSenderId(), e);
        }
    }

    /**
     * Async save to database (doesn't block WebSocket)
     */
    @Async
    protected CompletableFuture<Void> saveToDatabase(
            com.example.FoodDelivery.domain.res.websocket.ChatMessage websocketMessage) {
        try {
            // Convert WebSocket DTO to Database Entity
            ChatMessage entity = convertToEntity(websocketMessage);

            if (entity != null) {
                chatMessageRepository.save(entity);
                log.debug("💾 Async saved message to database: order={}", websocketMessage.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to async save message to database: order={}",
                    websocketMessage.getOrderId(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Get message history for an order
     * Strategy: Try Redis first → Fallback to Database if not cached
     * 
     * @param orderId the order ID
     * @param page    page number (0-indexed)
     * @param size    page size
     * @return list of messages (as WebSocket DTOs)
     */
    public List<com.example.FoodDelivery.domain.res.websocket.ChatMessage> getMessageHistory(
            Long orderId, int page, int size) {
        try {
            // Try Redis first
            if (redisChatService.isCached(orderId)) {
                List<com.example.FoodDelivery.domain.res.websocket.ChatMessage> cachedMessages = redisChatService
                        .getCachedMessages(orderId, page, size);
                if (!cachedMessages.isEmpty()) {
                    log.debug("🎯 CACHE HIT: Retrieved {} messages from Redis for order {}",
                            cachedMessages.size(), orderId);
                    return cachedMessages;
                }
            }

            // Fallback to Database
            log.debug("❌ CACHE MISS: Fetching from database for order {}", orderId);
            return getMessagesFromDatabase(orderId, page, size);
        } catch (Exception e) {
            log.error("Failed to get message history for order {}", orderId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get message count for an order
     * 
     * @param orderId the order ID
     * @return count of messages
     */
    public Long getMessageCount(Long orderId) {
        try {
            // Try Redis first
            if (redisChatService.isCached(orderId)) {
                Long count = redisChatService.getMessageCount(orderId);
                if (count > 0) {
                    return count;
                }
            }

            // Fallback to Database
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                return chatMessageRepository.countByOrder(order);
            }
            return 0L;
        } catch (Exception e) {
            log.error("Failed to get message count for order {}", orderId, e);
            return 0L;
        }
    }

    /**
     * Get messages from database and optionally cache them
     */
    private List<com.example.FoodDelivery.domain.res.websocket.ChatMessage> getMessagesFromDatabase(
            Long orderId, int page, int size) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("Order not found: {}", orderId);
                return new ArrayList<>();
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessage> messagePage = chatMessageRepository.findByOrderOrderBySentAtDesc(order, pageable);

            List<com.example.FoodDelivery.domain.res.websocket.ChatMessage> messages = messagePage.getContent()
                    .stream()
                    .map(this::convertToWebSocketDTO)
                    .collect(Collectors.toList());

            log.debug("📊 Retrieved {} messages from database for order {}", messages.size(), orderId);

            // Optionally: Re-cache to Redis for future requests
            // (This helps warm up the cache after it expires)
            if (!messages.isEmpty()) {
                messages.forEach(redisChatService::cacheMessage);
                log.debug("🔄 Re-cached {} messages to Redis for order {}", messages.size(), orderId);
            }

            return messages;
        } catch (Exception e) {
            log.error("Failed to get messages from database for order {}", orderId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Convert WebSocket DTO to Database Entity
     */
    private ChatMessage convertToEntity(com.example.FoodDelivery.domain.res.websocket.ChatMessage dto) {
        try {
            Order order = orderRepository.findById(dto.getOrderId()).orElse(null);
            User sender = userRepository.findById(dto.getSenderId()).orElse(null);

            if (order == null || sender == null) {
                log.warn("Cannot convert message to entity: order={}, sender={}",
                        dto.getOrderId(), dto.getSenderId());
                return null;
            }

            // Determine recipient based on sender type
            User recipient = null;
            if ("CUSTOMER".equals(dto.getSenderType()) && order.getDriver() != null) {
                recipient = order.getDriver();
            } else if ("DRIVER".equals(dto.getSenderType()) && order.getCustomer() != null) {
                recipient = order.getCustomer();
            }

            return ChatMessage.builder()
                    .order(order)
                    .sender(sender)
                    .recipient(recipient)
                    .messageContent(dto.getMessage())
                    .sentAt(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())
                    .isRead(false)
                    .build();
        } catch (Exception e) {
            log.error("Failed to convert WebSocket DTO to entity", e);
            return null;
        }
    }

    /**
     * Convert Database Entity to WebSocket DTO
     */
    private com.example.FoodDelivery.domain.res.websocket.ChatMessage convertToWebSocketDTO(ChatMessage entity) {
        com.example.FoodDelivery.domain.res.websocket.ChatMessage dto = new com.example.FoodDelivery.domain.res.websocket.ChatMessage();
        dto.setOrderId(entity.getOrder().getId());
        dto.setSenderId(entity.getSender().getId());
        dto.setSenderName(entity.getSender().getName());

        // Determine sender type based on user role or order relationship
        if (entity.getOrder().getCustomer() != null
                && entity.getSender().getId().equals(entity.getOrder().getCustomer().getId())) {
            dto.setSenderType("CUSTOMER");
        } else if (entity.getOrder().getDriver() != null
                && entity.getSender().getId().equals(entity.getOrder().getDriver().getId())) {
            dto.setSenderType("DRIVER");
        } else {
            dto.setSenderType("UNKNOWN");
        }

        dto.setMessage(entity.getMessageContent());
        dto.setTimestamp(entity.getSentAt());
        dto.setMessageType("TEXT"); // Default to TEXT

        return dto;
    }
}
