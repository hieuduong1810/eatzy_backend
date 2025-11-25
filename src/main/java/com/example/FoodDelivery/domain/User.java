package com.example.FoodDelivery.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.constant.GenderEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.core.util.Json;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @NotBlank(message = "Password không được để trống")
    private String password;

    @NotBlank(message = "Email không được để trống")
    private String email;
    private String phoneNumber;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    private String address;
    private Boolean isActive;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // Role relationship
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    // Customer Profile (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerProfile customerProfile;

    // Driver Profile (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DriverProfile driverProfile;

    // Wallet (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    // Addresses (One-to-Many for customers)
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Address> addresses;

    // Restaurants owned (One-to-Many for restaurant owners)
    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private List<Restaurant> ownedRestaurants;

    // Orders as customer (One-to-Many)
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Order> ordersAsCustomer;

    // Orders as driver (One-to-Many)
    @OneToMany(mappedBy = "driver")
    @JsonIgnore
    private List<Order> ordersAsDriver;

    // Voucher usages (One-to-Many)
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<OrderVoucherUsage> voucherUsages;

    // Reviews (One-to-Many)
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Review> reviews;

    // Favorites (One-to-Many)
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<CustomerFavorite> favorites;

    // Order earnings as driver (One-to-Many)
    @OneToMany(mappedBy = "driver")
    @JsonIgnore
    private List<OrderEarningsSummary> driverEarnings;

    // Chat messages sent (One-to-Many)
    @OneToMany(mappedBy = "sender")
    @JsonIgnore
    private List<ChatMessage> sentMessages;

    // Chat messages received (One-to-Many)
    @OneToMany(mappedBy = "recipient")
    @JsonIgnore
    private List<ChatMessage> receivedMessages;

    // System configurations updated (One-to-Many)
    @OneToMany(mappedBy = "lastUpdatedBy")
    @JsonIgnore
    private List<SystemConfiguration> systemConfigurations;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }
}
