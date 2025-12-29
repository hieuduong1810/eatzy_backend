package com.example.FoodDelivery.domain.res.cart;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResCartDTO {
    private Long id;
    private Customer customer;
    private Restaurant restaurant;
    private List<ResCartItemDTO> cartItems;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Customer {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Restaurant {
        private long id;
        private String name;
        private String address;
    }
}
