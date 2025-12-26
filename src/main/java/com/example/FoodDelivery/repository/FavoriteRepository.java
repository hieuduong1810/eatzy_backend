package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.Favorite;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository
                extends JpaRepository<Favorite, Long>, JpaSpecificationExecutor<Favorite> {
        List<Favorite> findByCustomerId(Long customerId);

        Optional<Favorite> findByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);

        boolean existsByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);
}
