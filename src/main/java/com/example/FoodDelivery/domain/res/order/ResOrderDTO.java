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
    private Customer customer;
    private Restaurant restaurant;
    private Driver driver;
    private List<Voucher> vouchers;
    private String orderStatus;
    private String deliveryAddress;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    private BigDecimal distance;
    private String specialInstructions;
    private BigDecimal subtotal;
    private BigDecimal restaurantCommissionAmount;
    private BigDecimal restaurantNetEarning;
    private BigDecimal deliveryFee;
    private BigDecimal driverCommissionAmount;
    private BigDecimal driverNetEarning;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String cancellationReason;
    private Instant createdAt;
    private Instant preparingAt;
    private Instant deliveredAt;
    private Long totalTripDuration; // Total trip duration in minutes
    private List<ResOrderItemDTO> orderItems;
    private String vnpayPaymentUrl; // VNPAY payment URL if payment method is VNPAY

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Customer {
        private long id;
        private String name;
        private String phoneNumber;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Driver {
        private long id;
        private String name;
        private String vehicleType;
        private String vehicleDetails;
        private String averageRating;
        private String completedTrips;
        private String vehicleLicensePlate;
        private String phoneNumber;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Restaurant {
        private long id;
        private String name;
        private String address;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Voucher {
        private long id;
        private String code;
    }
}
