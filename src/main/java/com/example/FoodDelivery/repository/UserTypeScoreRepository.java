package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.UserTypeScore;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTypeScoreRepository extends JpaRepository<UserTypeScore, Long> {
    
    /**
     * Find score by user and restaurant type
     */
    Optional<UserTypeScore> findByUserIdAndRestaurantTypeId(Long userId, Long restaurantTypeId);
    
    /**
     * Get all type scores for a user
     */
    List<UserTypeScore> findByUserId(Long userId);
    
    /**
     * Get scores for specific user and list of type IDs
     * Used for calculating recommendation score
     */
    @Query("SELECT uts FROM UserTypeScore uts WHERE uts.user.id = :userId AND uts.restaurantType.id IN :typeIds")
    List<UserTypeScore> findByUserIdAndTypeIds(@Param("userId") Long userId, @Param("typeIds") List<Long> typeIds);
    
    /**
     * Calculate total type score for a user across multiple restaurant types
     * Returns sum of scores for all types that the user has interacted with
     */
    @Query("SELECT COALESCE(SUM(uts.score), 0) FROM UserTypeScore uts " +
           "WHERE uts.user.id = :userId AND uts.restaurantType.id IN :typeIds")
    Integer getTotalTypeScoreForUser(@Param("userId") Long userId, @Param("typeIds") List<Long> typeIds);
}
