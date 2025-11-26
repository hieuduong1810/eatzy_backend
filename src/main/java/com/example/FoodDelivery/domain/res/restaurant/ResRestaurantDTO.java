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
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contactPhone;
    private String status;
    private BigDecimal commissionRate;
    private BigDecimal averageRating;
    private String schedule;
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
