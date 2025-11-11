package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(precision = 15, scale = 2)
    private BigDecimal pendingBalance;

    @OneToMany(mappedBy = "wallet")
    private List<WalletTransaction> transactions;
}