package com.b4code.backend.models;

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
    private String name;

    @Column(nullable = false)
    private String roomType;

    @Column(nullable = false)
    private Integer maxOccupancy;

    private Integer sqft;

    private String bedType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    private String tag;

    private String imageSrc;

    private String features;

    private String amenities;

    @Builder.Default
    private Boolean available = true;
}
