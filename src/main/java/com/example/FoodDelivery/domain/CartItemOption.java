package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "cart_item_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_option_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_item_id", nullable = false)
    @JsonIgnore
    private CartItem cartItem;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    private MenuOption menuOption;
}
