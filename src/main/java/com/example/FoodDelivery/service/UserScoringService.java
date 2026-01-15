package com.example.FoodDelivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.RestaurantType;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.UserRestaurantScore;
import com.example.FoodDelivery.domain.UserTypeScore;
import com.example.FoodDelivery.repository.UserRestaurantScoreRepository;
import com.example.FoodDelivery.repository.UserTypeScoreRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Service to track user behavior and update scoring for restaurant recommendations
 * Based on scoring rules defined in the business requirements
 */
@Service
@Slf4j
public class UserScoringService {
    
    private final UserTypeScoreRepository userTypeScoreRepository;
    private final UserRestaurantScoreRepository userRestaurantScoreRepository;

    public UserScoringService(
            UserTypeScoreRepository userTypeScoreRepository,
            UserRestaurantScoreRepository userRestaurantScoreRepository) {
        this.userTypeScoreRepository = userTypeScoreRepository;
        this.userRestaurantScoreRepository = userRestaurantScoreRepository;
    }

    /**
     * Update score when user searches for restaurant by name and clicks on it
     * Restaurant Score: +2, Type Score: +2
     */
    @Transactional
    public void trackSearchRestaurantByNameAndClick(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, 2);
        updateTypeScores(user.getId(), restaurant.getRestaurantTypes(), 2);
        log.info("👤 User {} searched and clicked restaurant {}: +2 points", user.getId(), restaurant.getId());
    }

    /**
     * Update score when user searches for dish/food type and clicks on restaurant
     * Restaurant Score: 0, Type Score: +2
     */
    @Transactional
    public void trackSearchDishAndClick(User user, Restaurant restaurant) {
        updateTypeScores(user.getId(), restaurant.getRestaurantTypes(), 2);
        log.info("👤 User {} searched dish and clicked restaurant {}: +2 type points", user.getId(), restaurant.getId());
    }

    /**
     * Update score when user clicks to view restaurant details (from home/list)
     * Restaurant Score: +1, Type Score: +1
     */
    @Transactional
    public void trackViewRestaurantDetails(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, 1);
        updateTypeScores(user.getId(), restaurant.getRestaurantTypes(), 1);
        log.info("👤 User {} viewed restaurant {}: +1 point", user.getId(), restaurant.getId());
    }

    /**
     * Update score when user adds items to cart
     * Restaurant Score: +3, Type Score: +3
     */
    @Transactional
    public void trackAddToCart(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, 3);
        updateTypeScores(user.getId(), restaurant.getRestaurantTypes(), 3);
        log.info("👤 User {} added to cart from restaurant {}: +3 points", user.getId(), restaurant.getId());
    }

    /**
     * Update score when user places an order
     * Restaurant Score: +10, Type Score: +5
     */
    @Transactional
    public void trackPlaceOrder(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, 10);
        updateTypeScores(user.getId(), restaurant.getRestaurantTypes(), 5);
        log.info("👤 User {} placed order at restaurant {}: +10 restaurant, +5 type points", 
                user.getId(), restaurant.getId());
    }

    /**
     * Update score when user rates 5 stars (Excellent)
     * Restaurant Score: +5, Type Score: +3
     */
    @Transactional
    public void trackRating5Stars(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, 5);
        updateTypeScores(user.getId(), restaurant.getRestaurantTypes(), 3);
        log.info("⭐⭐⭐⭐⭐ User {} rated restaurant {} 5 stars: +5 restaurant, +3 type points", 
                user.getId(), restaurant.getId());
    }

    /**
     * Update score when user rates 4 stars (Good)
     * Restaurant Score: +3, Type Score: +1
     */
    @Transactional
    public void trackRating4Stars(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, 3);
        updateTypeScores(user.getId(), restaurant.getRestaurantTypes(), 1);
        log.info("⭐⭐⭐⭐ User {} rated restaurant {} 4 stars: +3 restaurant, +1 type points", 
                user.getId(), restaurant.getId());
    }

    /**
     * Update score when user rates 3 stars (Average)
     * Restaurant Score: 0, Type Score: 0 (no change)
     */
    @Transactional
    public void trackRating3Stars(User user, Restaurant restaurant) {
        log.info("⭐⭐⭐ User {} rated restaurant {} 3 stars: no score change", 
                user.getId(), restaurant.getId());
        // No score update for average rating
    }

    /**
     * Update score when user rates 2 stars (Bad)
     * Restaurant Score: -10, Type Score: 0 (bad restaurant, not bad food type)
     */
    @Transactional
    public void trackRating2Stars(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, -10);
        log.info("⭐⭐ User {} rated restaurant {} 2 stars: -10 restaurant points", 
                user.getId(), restaurant.getId());
    }

    /**
     * Update score when user rates 1 star (Terrible)
     * Restaurant Score: -50, Type Score: 0 (very bad restaurant, not bad food type)
     */
    @Transactional
    public void trackRating1Star(User user, Restaurant restaurant) {
        updateRestaurantScore(user.getId(), restaurant, -50);
        log.info("⭐ User {} rated restaurant {} 1 star: -50 restaurant points", 
                user.getId(), restaurant.getId());
    }

    /**
     * Generic method to handle rating based on star count
     */
    @Transactional
    public void trackRating(User user, Restaurant restaurant, Integer stars) {
        switch (stars) {
            case 5:
                trackRating5Stars(user, restaurant);
                break;
            case 4:
                trackRating4Stars(user, restaurant);
                break;
            case 3:
                trackRating3Stars(user, restaurant);
                break;
            case 2:
                trackRating2Stars(user, restaurant);
                break;
            case 1:
                trackRating1Star(user, restaurant);
                break;
            default:
                log.warn("Invalid rating stars: {}", stars);
        }
    }

    /**
     * Update restaurant score (loyalty/familiarity)
     */
    private void updateRestaurantScore(Long userId, Restaurant restaurant, int points) {
        UserRestaurantScore score = userRestaurantScoreRepository
                .findByUserIdAndRestaurantId(userId, restaurant.getId())
                .orElseGet(() -> {
                    UserRestaurantScore newScore = new UserRestaurantScore();
                    User user = new User();
                    user.setId(userId);
                    newScore.setUser(user);
                    newScore.setRestaurant(restaurant);
                    newScore.setScore(0);
                    return newScore;
                });
        
        score.setScore(score.getScore() + points);
        userRestaurantScoreRepository.save(score);
    }

    /**
     * Update type scores for all restaurant types
     */
    private void updateTypeScores(Long userId, List<RestaurantType> types, int points) {
        if (types == null || types.isEmpty()) {
            return;
        }

        for (RestaurantType type : types) {
            UserTypeScore score = userTypeScoreRepository
                    .findByUserIdAndRestaurantTypeId(userId, type.getId())
                    .orElseGet(() -> {
                        UserTypeScore newScore = new UserTypeScore();
                        User user = new User();
                        user.setId(userId);
                        newScore.setUser(user);
                        newScore.setRestaurantType(type);
                        newScore.setScore(0);
                        return newScore;
                    });
            
            score.setScore(score.getScore() + points);
            userTypeScoreRepository.save(score);
        }
    }
}
