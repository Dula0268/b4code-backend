package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private String roomType; // e.g., Deluxe Suite, Standard Double

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal basePrice;

    private String weekendPercentage; // e.g., +15%

    @Column(nullable = false)
    private String status; // ACTIVE, DRAFT

    private Boolean weekendFridaySaturday;
    private Integer weekendMultiplierPercent;
    private Boolean sundayNight;
    private Integer sundayMultiplierPercent;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
