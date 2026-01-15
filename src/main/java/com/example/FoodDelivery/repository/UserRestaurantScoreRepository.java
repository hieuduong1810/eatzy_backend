package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.UserRestaurantScore;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRestaurantScoreRepository extends JpaRepository<UserRestaurantScore, Long> {
    
    /**
     * Find score by user and restaurant
     */
    Optional<UserRestaurantScore> findByUserIdAndRestaurantId(Long userId, Long restaurantId);
    
    /**
     * Get all restaurant scores for a user
     */
    List<UserRestaurantScore> findByUserId(Long userId);
    
    /**
     * Get score for specific user and restaurant
     * Returns 0 if not found
     */
    default Integer getScoreOrDefault(Long userId, Long restaurantId) {
        return findByUserIdAndRestaurantId(userId, restaurantId)
                .map(UserRestaurantScore::getScore)
                .orElse(0);
    }
}
