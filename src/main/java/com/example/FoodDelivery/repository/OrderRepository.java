package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Count how many times a customer has used a specific voucher (with
    // many-to-many relationship)
    @Query("SELECT COUNT(o) FROM Order o JOIN o.vouchers v WHERE o.customer.id = :customerId AND v.id = :voucherId")
    Long countByCustomerIdAndVoucherId(@Param("customerId") Long customerId, @Param("voucherId") Long voucherId);

    List<Order> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Order> findByPaymentMethodAndPaymentStatusAndCreatedAtBefore(String paymentMethod, String paymentStatus,
            Instant createdAt);

    List<Order> findByOrderStatusAndCreatedAtBefore(String orderStatus, Instant createdAt);

    List<Order> findByOrderStatusAndPreparingAtBefore(String orderStatus, Instant preparingAt);

    List<Order> findByOrderStatusAndDriverIsNullOrderByPreparingAtAsc(String orderStatus);

    // Count orders waiting for driver (status CONFIRMED or PREPARING with no
    // driver)
    long countByOrderStatusInAndDriverIsNull(java.util.List<String> statuses);

    // Find orders that have been assigned to driver but not accepted yet and exceed
    // timeout
    List<Order> findByOrderStatusAndDriverIsNotNullAndAssignedAtBefore(String orderStatus, Instant assignedAt);

    // Report queries
    List<Order> findByRestaurantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long restaurantId, Instant startDate, Instant endDate);

    Long countByRestaurantIdAndOrderStatusAndCreatedAtBetween(
            Long restaurantId, String status, Instant startDate, Instant endDate);
}
