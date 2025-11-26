package com.example.FoodDelivery.domain.res.order;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResOrderItemOptionDTO {
    private Long id;
    private MenuOption menuOption;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuOption {
        private long id;
        private String name;
        private BigDecimal priceAdjustment;
    }
}
