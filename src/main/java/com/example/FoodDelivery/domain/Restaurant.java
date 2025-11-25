package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @Column(unique = true)
    private String slug; // SEO-friendly URL: "nha-hang-pho-ha-noi"

    @Column(length = 70)
    private String metaTitle; // SEO title (max 70 chars for Google)

    @Column(length = 160)
    private String metaDescription; // SEO description (max 160 chars)

    @Column(columnDefinition = "TEXT")
    private String metaKeywords; // SEO keywords: "phở, bún bò, món Việt"

    private String ogImage; // Open Graph image URL for social sharing

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

    private String schedule; // e.g., "09:00-21:00"

    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    private List<DishCategory> dishCategories;

    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    private List<Dish> dishes;

    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    private List<Order> orders;

    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    private List<Voucher> vouchers;

    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    private List<MonthlyRevenueReport> monthlyRevenueReports;

    @ManyToMany
    @JsonIgnoreProperties("restaurants")
    @JoinTable(name = "restaurant_restaurant_type", joinColumns = @JoinColumn(name = "restaurant_id"), inverseJoinColumns = @JoinColumn(name = "restaurant_type_id"))
    private List<RestaurantType> restaurantTypes;
}