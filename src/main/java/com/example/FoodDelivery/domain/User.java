package com.example.FoodDelivery.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.constant.GenderEnum;

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

    // Driver Document (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DriverDocument driverDocument;

    // Wallet (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    // Addresses (One-to-Many for customers)
    @OneToMany(mappedBy = "customer")
    private List<Address> addresses;

    // Restaurants owned (One-to-Many for restaurant owners)
    @OneToMany(mappedBy = "owner")
    private List<Restaurant> ownedRestaurants;

    // Carts (One-to-Many)
    @OneToMany(mappedBy = "customer")
    private List<Cart> carts;

    // Orders as customer (One-to-Many)
    @OneToMany(mappedBy = "customer")
    private List<Order> ordersAsCustomer;

    // Orders as driver (One-to-Many)
    @OneToMany(mappedBy = "driver")
    private List<Order> ordersAsDriver;

    // Voucher usages (One-to-Many)
    @OneToMany(mappedBy = "customer")
    private List<OrderVoucherUsage> voucherUsages;

    // Reviews (One-to-Many)
    @OneToMany(mappedBy = "customer")
    private List<Review> reviews;

    // Favorites (One-to-Many)
    @OneToMany(mappedBy = "customer")
    private List<CustomerFavorite> favorites;

    // Order earnings as driver (One-to-Many)
    @OneToMany(mappedBy = "driver")
    private List<OrderEarningsSummary> driverEarnings;

    // Chat messages sent (One-to-Many)
    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> sentMessages;

    // Chat messages received (One-to-Many)
    @OneToMany(mappedBy = "recipient")
    private List<ChatMessage> receivedMessages;

    // System configurations updated (One-to-Many)
    @OneToMany(mappedBy = "lastUpdatedBy")
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
