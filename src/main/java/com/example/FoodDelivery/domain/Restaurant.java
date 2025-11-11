package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    private String contactPhone;
    private String status;

    @Column(precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(columnDefinition = "JSON")
    private String schedule;

    @OneToMany(mappedBy = "restaurant")
    private List<DishCategory> dishCategories;

    @OneToMany(mappedBy = "restaurant")
    private List<Dish> dishes;

    @OneToMany(mappedBy = "restaurant")
    private List<Order> orders;

    @OneToMany(mappedBy = "restaurant")
    private List<Voucher> vouchers;
}