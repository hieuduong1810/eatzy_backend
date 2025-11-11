package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "driver_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {
    @Id
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String vehicleDetails;
    private String status;

    @Column(precision = 10, scale = 8)
    private BigDecimal currentLatitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal currentLongitude;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;
}