package com.example.FoodDelivery.domain.res.order;

import java.math.BigDecimal;

public class ResDeliveryFeeDTO {

    private BigDecimal deliveryFee;
    private BigDecimal distance;
    private BigDecimal surgeMultiplier;
    private BigDecimal baseFee;
    private BigDecimal baseDistance;
    private BigDecimal perKmFee;

    // Constructors
    public ResDeliveryFeeDTO() {
    }

    public ResDeliveryFeeDTO(BigDecimal deliveryFee, BigDecimal distance, BigDecimal surgeMultiplier,
            BigDecimal baseFee, BigDecimal baseDistance, BigDecimal perKmFee) {
        this.deliveryFee = deliveryFee;
        this.distance = distance;
        this.surgeMultiplier = surgeMultiplier;
        this.baseFee = baseFee;
        this.baseDistance = baseDistance;
        this.perKmFee = perKmFee;
    }

    // Getters and Setters
    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public BigDecimal getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public void setSurgeMultiplier(BigDecimal surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee;
    }

    public BigDecimal getBaseDistance() {
        return baseDistance;
    }

    public void setBaseDistance(BigDecimal baseDistance) {
        this.baseDistance = baseDistance;
    }

    public BigDecimal getPerKmFee() {
        return perKmFee;
    }

    public void setPerKmFee(BigDecimal perKmFee) {
        this.perKmFee = perKmFee;
    }
}
