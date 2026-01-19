package com.example.FoodDelivery.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.ChatMessage;
import com.example.FoodDelivery.domain.Order;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all chat messages for a specific order, ordered by sentAt descending
     * (most recent first)
     * 
     * @param order    the order to find messages for
     * @param pageable pagination information
     * @return page of chat messages
     */
    Page<ChatMessage> findByOrderOrderBySentAtDesc(Order order, Pageable pageable);

    /**
     * Count total messages for a specific order
     * 
     * @param order the order to count messages for
     * @return total count of messages
     */
    Long countByOrder(Order order);
}
