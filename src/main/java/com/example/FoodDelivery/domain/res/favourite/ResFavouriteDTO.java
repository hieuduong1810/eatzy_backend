package com.example.FoodDelivery.domain.res.favourite;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResFavouriteDTO {
    private Long id;
    private User customer;
    private Restaurant restaurant;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Restaurant {
        private Long id;
        private String name;
        private String slug;
        private String address;
        private String description;
        private BigDecimal averageRating;
        private String imageUrl;
        private List<RestaurantType> restaurantTypes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantType {
        private Long id;
        private String name;
    }
}
