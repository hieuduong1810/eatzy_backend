package com.example.FoodDelivery.domain.req;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ReqOrderDTO {

    @NotNull(message = "Customer không được để trống")
    private Customer customer;

    @NotNull(message = "Restaurant không được để trống")
    private Restaurant restaurant;

    private Driver driver;

    private String orderStatus;

    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    private String deliveryAddress;

    @NotNull(message = "Latitude không được để trống")
    private Double deliveryLatitude;

    @NotNull(message = "Longitude không được để trống")
    private Double deliveryLongitude;

    private String specialInstructions;

    private BigDecimal subtotal;

    @NotNull(message = "Phí giao hàng không được để trống")
    private BigDecimal deliveryFee;

    private BigDecimal totalAmount;

    private String paymentMethod;
    private String paymentStatus;
    private String cancellationReason;
    private Instant createdAt;
    private Instant deliveredAt;

    @NotNull(message = "Order items không được để trống")
    private List<OrderItem> orderItems;

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

    public static class Driver {
        private Long id;

        public Driver() {
        }

        public Driver(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class OrderItem {
        private Long id;

        @NotNull(message = "Dish không được để trống")
        private Dish dish;

        @NotNull(message = "Quantity không được để trống")
        private Integer quantity;

        private List<OrderItemOption> orderItemOptions;

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

        public static class OrderItemOption {
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

            public OrderItemOption() {
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

        public List<OrderItemOption> getOrderItemOptions() {
            return orderItemOptions;
        }

        public void setOrderItemOptions(List<OrderItemOption> orderItemOptions) {
            this.orderItemOptions = orderItemOptions;
        }
    }

    // Constructors
    public ReqOrderDTO() {
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

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(Double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(Double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
