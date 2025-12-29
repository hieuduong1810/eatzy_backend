package com.example.FoodDelivery.domain.req;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReqCartDTO {

    @NotNull(message = "Customer không được để trống")
    private Customer customer;

    @NotNull(message = "Restaurant không được để trống")
    private Restaurant restaurant;

    private List<CartItem> cartItems;

    // Nested classes
    public static class Customer {
        @NotNull(message = "Customer ID không được để trống")
        private Long id;

        public Customer() {
        }

        public Customer(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class Restaurant {
        @NotNull(message = "Restaurant ID không được để trống")
        private Long id;

        public Restaurant() {
        }

        public Restaurant(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class CartItem {
        private Long id;

        @NotNull(message = "Dish không được để trống")
        private Dish dish;

        @NotNull(message = "Quantity không được để trống")
        private Integer quantity;

        private List<CartItemOption> cartItemOptions;

        public static class Dish {
            @NotNull(message = "Dish ID không được để trống")
            private Long id;

            public Dish() {
            }

            public Dish(Long id) {
                this.id = id;
            }

            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }
        }

        public static class CartItemOption {
            private Long id;

            @NotNull(message = "Menu Option không được để trống")
            private MenuOption menuOption;

            public static class MenuOption {
                @NotNull(message = "Menu Option ID không được để trống")
                private Long id;

                public MenuOption() {
                }

                public MenuOption(Long id) {
                    this.id = id;
                }

                public Long getId() {
                    return id;
                }

                public void setId(Long id) {
                    this.id = id;
                }
            }

            public CartItemOption() {
            }

            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }

            public MenuOption getMenuOption() {
                return menuOption;
            }

            public void setMenuOption(MenuOption menuOption) {
                this.menuOption = menuOption;
            }
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Dish getDish() {
            return dish;
        }

        public void setDish(Dish dish) {
            this.dish = dish;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public List<CartItemOption> getCartItemOptions() {
            return cartItemOptions;
        }

        public void setCartItemOptions(List<CartItemOption> cartItemOptions) {
            this.cartItemOptions = cartItemOptions;
        }
    }

    // Constructors
    public ReqCartDTO() {
    }

    // Getters and Setters
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
}
