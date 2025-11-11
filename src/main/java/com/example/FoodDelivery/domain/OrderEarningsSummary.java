package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_earnings_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEarningsSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(precision = 10, scale = 2)
    private BigDecimal orderSubtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(precision = 5, scale = 2)
    private BigDecimal restaurantCommissionRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal restaurantCommissionAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal restaurantNetEarning;

    @Column(precision = 5, scale = 2)
    private BigDecimal driverCommissionRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal driverCommissionAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal driverNetEarning;

    @Column(precision = 10, scale = 2)
    private BigDecimal platformTotalEarning;

    private Instant recordedAt;
}