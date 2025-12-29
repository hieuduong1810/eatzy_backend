package com.example.FoodDelivery.domain.res.restaurant;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResRestaurantMagazineDTO {
    private Long id;
    private String name;
    private String slug;
    private String address;
    private String description;
    private Integer oneStarCount;
    private Integer twoStarCount;
    private Integer threeStarCount;
    private Integer fourStarCount;
    private Integer fiveStarCount;
    private BigDecimal averageRating;
    private BigDecimal distance;
    private List<Category> category;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Category {
        private Long id;
        private String name;
        private List<Dish> dishes;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Dish {
            private Long id;
            private String name;
            private String description;
            private BigDecimal price;
            private String imageUrl;
        }
    }
}
