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

@Entity(name = "OwnerRoom")
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String roomType; // STANDARD, DELUXE, SUITE, KING_SUITE, SINGLE_STUDIO, FAMILY_LOFT, PENTHOUSE

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE, BLOCKED

    @Column(precision = 10, scale = 2)
    private BigDecimal baseRate;

    private Integer maxOccupancy;

    // ── New fields for room management ──

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer maxAdults;
    private Integer maxChildren;
    private String currency; // LKR, USD, EUR, etc.

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
