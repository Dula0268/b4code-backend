package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "seasonal_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private String name; // e.g., Summer Peak, Winter Season

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String percentage; // e.g., +20%, -10%

    private Integer progress; // 0-100
}
