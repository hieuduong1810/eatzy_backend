package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "driver_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String documentUrl;

    private String verificationStatus;
}