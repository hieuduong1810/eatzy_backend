package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    private String orderStatus;

    @Column(columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(precision = 10, scale = 8)
    private BigDecimal deliveryLatitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal deliveryLongitude;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    private String paymentMethod;
    private String paymentStatus;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    private Instant createdAt;
    private Instant preparingAt;
    private Instant deliveredAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order")
    private List<OrderVoucherUsage> voucherUsages;

    @OneToMany(mappedBy = "order")
    private List<Review> reviews;

    @OneToMany(mappedBy = "order")
    private List<ChatMessage> chatMessages;
}