package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    private Integer quantity;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private List<OrderItemOption> orderItemOptions;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

}