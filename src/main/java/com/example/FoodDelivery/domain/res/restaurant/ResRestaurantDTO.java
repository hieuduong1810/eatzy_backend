package com.example.FoodDelivery.domain.res.restaurant;

import java.math.BigDecimal;
import java.util.List;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.DishCategory;
import com.example.FoodDelivery.domain.MonthlyRevenueReport;
import com.example.FoodDelivery.domain.Voucher;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResRestaurantDTO {
    private Long id;
    private User owner;
    private String name;
    private String slug;
    private String address;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contactPhone;
    private String status;
    private BigDecimal commissionRate;
    private Integer oneStarCount;
    private Integer twoStarCount;
    private Integer threeStarCount;
    private Integer fourStarCount;
    private Integer fiveStarCount;
    private BigDecimal averageRating;
    private Integer reviewCount; // Total number of reviews
    private String schedule;
    private String avatarUrl; // Profile image
    private String coverImageUrl; // Background image
    private BigDecimal distance; // Distance in km from user location
    private RestaurantType restaurantTypes;

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
    public static class RestaurantType {
        private long id;
        private String name;
    }
}
