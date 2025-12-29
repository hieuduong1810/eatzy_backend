package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.Cart;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {
    List<Cart> findByCustomerId(Long customerId);

    List<Cart> findByRestaurantId(Long restaurantId);

    Optional<Cart> findByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);

    List<Cart> findByCustomerIdOrderByIdDesc(Long customerId);
}
