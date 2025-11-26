package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "menu_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private MenuOptionGroup menuOptionGroup;

    private String name;

    @Column(name = "price_adjustment", precision = 10, scale = 2)
    private BigDecimal priceAdjustment;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @OneToMany(mappedBy = "menuOption")
    private List<OrderItemOption> orderItemOptions;
}
