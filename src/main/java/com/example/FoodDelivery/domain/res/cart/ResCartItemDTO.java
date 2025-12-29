package com.example.FoodDelivery.domain.res.cart;

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
public class ResCartItemDTO {
    private Long id;
    private Dish dish;
    private Integer quantity;
    private List<ResCartItemOptionDTO> cartItemOptions;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Dish {
        private long id;
        private String name;
        private BigDecimal price;
        private String image;
    }
}
