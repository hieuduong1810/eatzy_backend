package com.example.FoodDelivery.domain.res.order;

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
public class ResOrderItemDTO {
    private Long id;
    private Dish dish;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private List<ResOrderItemOptionDTO> orderItemOptions;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Dish {
        private long id;
        private String name;
        private BigDecimal price;
    }
}
