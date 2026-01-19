package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.res.websocket.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis service for caching chat messages
 * Uses Redis Lists for storing message history per order
 * TTL: 7 days (604800 seconds)
 */
@Service
@Slf4j
public class RedisChatService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHAT_KEY_PREFIX = "chat:order:";
    private static final String CHAT_KEY_SUFFIX = ":messages";
    private static final long TTL_DAYS = 7;
    private static final long TTL_SECONDS = TTL_DAYS * 24 * 60 * 60; // 7 days in seconds

    public RedisChatService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Cache a chat message to Redis
     * Stores as JSON string in a Redis List
     * 
     * @param message the chat message to cache
     */
    public void cacheMessage(ChatMessage message) {
        try {
            String key = buildKey(message.getOrderId());
            String messageJson = objectMapper.writeValueAsString(message);

            // Add message to the end of the list (RPUSH)
            redisTemplate.opsForList().rightPush(key, messageJson);

            // Set TTL for the entire list (7 days)
            redisTemplate.expire(key, TTL_SECONDS, TimeUnit.SECONDS);

            log.debug("💾 Cached message to Redis: order={}, key={}, TTL={}s",
                    message.getOrderId(), key, TTL_SECONDS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message to JSON for Redis caching", e);
        } catch (Exception e) {
            log.error("Failed to cache message to Redis", e);
        }
    }

    /**
     * Get cached messages from Redis with pagination
     * 
     * @param orderId the order ID
     * @param page    page number (0-indexed)
     * @param size    page size
     * @return list of cached messages
     */
    public List<ChatMessage> getCachedMessages(Long orderId, int page, int size) {
        try {
            String key = buildKey(orderId);

            // Get total count
            Long total = redisTemplate.opsForList().size(key);
            if (total == null || total == 0) {
                log.debug("No cached messages found in Redis for order {}", orderId);
                return new ArrayList<>();
            }

            // Calculate range for pagination
            // Redis lists are 0-indexed, and we want newest first
            // List is stored oldest to newest (RPUSH adds to end)
            // So we need to read from the end backwards
            long start = Math.max(0, total - (page + 1) * size);
            long end = total - page * size - 1;

            if (start > end || end < 0) {
                return new ArrayList<>();
            }

            // Get messages from Redis (LRANGE)
            List<Object> rawMessages = redisTemplate.opsForList().range(key, start, end);
            if (rawMessages == null || rawMessages.isEmpty()) {
                return new ArrayList<>();
            }

            // Deserialize and reverse (to get newest first)
            List<ChatMessage> messages = new ArrayList<>();
            for (int i = rawMessages.size() - 1; i >= 0; i--) {
                try {
                    ChatMessage msg = objectMapper.readValue(rawMessages.get(i).toString(), ChatMessage.class);
                    messages.add(msg);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize message from Redis", e);
                }
            }

            log.debug("📥 Retrieved {} cached messages from Redis for order {} (page={}, size={})",
                    messages.size(), orderId, page, size);

            return messages;
        } catch (Exception e) {
            log.error("Failed to get cached messages from Redis for order {}", orderId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get count of cached messages
     * 
     * @param orderId the order ID
     * @return count of messages, or 0 if not found
     */
    public Long getMessageCount(Long orderId) {
        try {
            String key = buildKey(orderId);
            Long count = redisTemplate.opsForList().size(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to get message count from Redis for order {}", orderId, e);
            return 0L;
        }
    }

    /**
     * Check if messages are cached for an order
     * 
     * @param orderId the order ID
     * @return true if cache exists
     */
    public boolean isCached(Long orderId) {
        try {
            String key = buildKey(orderId);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check cache existence for order {}", orderId, e);
            return false;
        }
    }

    /**
     * Build Redis key for order chat messages
     * 
     * @param orderId the order ID
     * @return Redis key
     */
    private String buildKey(Long orderId) {
        return CHAT_KEY_PREFIX + orderId + CHAT_KEY_SUFFIX;
    }
}
