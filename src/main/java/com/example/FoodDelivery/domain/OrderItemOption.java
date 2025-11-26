package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(name = "option_name")
    private String optionName;

    @Column(name = "price_at_purchase", precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "menu_option_id")
    private MenuOption menuOption;
}
