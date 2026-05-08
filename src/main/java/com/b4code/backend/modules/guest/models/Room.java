package com.b4code.backend.modules.guest.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private String name;           // e.g. "Deluxe Double"

    @Column(nullable = false)
    private String roomType;       // SINGLE, DOUBLE, SUITE, etc.

    @Column(nullable = false)
    private Integer maxOccupancy;

    private Integer sqft;
    
    private String bedType;        // e.g. "1 King Bed"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    private String tag;            // e.g. "Refundable", "Popular", "Last rooms"

    private String imageSrc;       // URL to room image

    private String features;      // e.g. "Private Balcony,Nespresso Machine"
    
    private String amenities;      // Generic room amenities

    @Builder.Default
    private Boolean available = true;
}