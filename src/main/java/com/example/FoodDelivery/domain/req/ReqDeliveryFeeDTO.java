package com.example.FoodDelivery.domain.req;

import jakarta.validation.constraints.NotNull;

public class ReqDeliveryFeeDTO {

    @NotNull(message = "Restaurant ID không được để trống")
    private Long restaurantId;

    @NotNull(message = "Latitude giao hàng không được để trống")
    private Double deliveryLatitude;

    @NotNull(message = "Longitude giao hàng không được để trống")
    private Double deliveryLongitude;

    // Constructors
    public ReqDeliveryFeeDTO() {
    }

    public ReqDeliveryFeeDTO(Long restaurantId, Double deliveryLatitude, Double deliveryLongitude) {
        this.restaurantId = restaurantId;
        this.deliveryLatitude = deliveryLatitude;
        this.deliveryLongitude = deliveryLongitude;
    }

    // Getters and Setters
    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(Double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(Double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }
}
