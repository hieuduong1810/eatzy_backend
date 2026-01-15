package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.Order;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByCustomerId(Long customerId);

    List<Order> findByRestaurantId(Long restaurantId);

    List<Order> findByDriverId(Long driverId);

    List<Order> findByOrderStatus(String orderStatus);

    List<Order> findByCustomerIdAndOrderStatus(Long customerId, String orderStatus);

    List<Order> findByRestaurantIdAndOrderStatus(Long restaurantId, String orderStatus);

    List<Order> findByDriverIdAndOrderStatus(Long driverId, String orderStatus);

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Count how many times a customer has used a specific voucher
    Long countByCustomerIdAndVoucherId(Long customerId, Long voucherId);

    List<Order> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Order> findByPaymentMethodAndPaymentStatusAndCreatedAtBefore(String paymentMethod, String paymentStatus,
            Instant createdAt);

    List<Order> findByOrderStatusAndCreatedAtBefore(String orderStatus, Instant createdAt);

    List<Order> findByOrderStatusAndPreparingAtBefore(String orderStatus, Instant preparingAt);

    List<Order> findByOrderStatusAndDriverIsNullOrderByPreparingAtAsc(String orderStatus);
}
