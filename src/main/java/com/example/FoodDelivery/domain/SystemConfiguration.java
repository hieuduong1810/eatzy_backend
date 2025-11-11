package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfiguration {
    @Id
    private String configKey;

    @Column(columnDefinition = "TEXT")
    private String configValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;
}