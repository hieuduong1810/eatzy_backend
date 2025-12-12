package com.example.FoodDelivery.domain.res.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResOrderDTO {
    private Long id;
    private User customer;
    private Restaurant restaurant;
    private User driver;
    private String orderStatus;
    private String deliveryAddress;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    private String specialInstructions;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String cancellationReason;
    private Instant createdAt;
    private Instant preparingAt;
    private Instant deliveredAt;
    private List<ResOrderItemDTO> orderItems;
    private String vnpayPaymentUrl; // VNPAY payment URL if payment method is VNPAY

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Restaurant {
        private long id;
        private String name;
    }
}
