package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private DishCategory category;

    private String name;

    @Column(unique = true)
    private String slug; // SEO-friendly URL: "bun-bo-hue-dac-biet"

    @Column(length = 70)
    private String metaTitle; // SEO title

    @Column(length = 160)
    private String metaDescription; // SEO description

    @Column(columnDefinition = "TEXT")
    private String metaKeywords; // SEO keywords: "bún bò Huế, món cay, món Huế"

    private String ogImage; // Open Graph image URL (can use imageUrl if not set)

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private String imageUrl;
    private int availabilityQuantity;

    @OneToMany(mappedBy = "dish")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "dish")
    private List<OrderItem> orderItems;
}