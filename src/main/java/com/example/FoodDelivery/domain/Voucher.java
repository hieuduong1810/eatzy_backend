package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String discountType;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    private Integer usageLimitPerUser;
    private Instant startDate;
    private Instant endDate;
    private Integer totalQuantity;

    @ManyToMany
    @JsonIgnoreProperties("vouchers")
    @JoinTable(name = "voucher_restaurant", joinColumns = @JoinColumn(name = "voucher_id"), inverseJoinColumns = @JoinColumn(name = "restaurant_id"))
    private List<Restaurant> restaurants;

    @ManyToMany
    @JsonIgnoreProperties("vouchers")
    @JoinTable(name = "voucher_order", joinColumns = @JoinColumn(name = "voucher_id"), inverseJoinColumns = @JoinColumn(name = "order_id"))
    private List<Order> orders;
}