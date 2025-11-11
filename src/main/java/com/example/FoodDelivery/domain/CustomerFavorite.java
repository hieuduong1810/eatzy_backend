package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    private String targetType;
    private Long targetId;
}