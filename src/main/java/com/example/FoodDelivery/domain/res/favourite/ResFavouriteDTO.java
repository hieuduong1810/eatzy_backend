package com.example.FoodDelivery.domain.res.favourite;

import java.math.BigDecimal;

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
        private String address;
        private String description;
        private BigDecimal averageRating;
        private String imageUrl;
    }
}
