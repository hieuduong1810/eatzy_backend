package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.CartItemOption;

import java.util.List;

@Repository
public interface CartItemOptionRepository
        extends JpaRepository<CartItemOption, Long>, JpaSpecificationExecutor<CartItemOption> {
    List<CartItemOption> findByCartItemId(Long cartItemId);
}
