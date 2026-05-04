package com.b4code.backend.modules.guest.entity;

import com.b4code.backend.modules.admin.models.Property;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    private String name;
    private Integer maxGuests;
    private String bedType;
    private Integer sqft;
    private Double pricePerNight;
    private Double originalPrice;
    private String tag;
    private String imageUrl;
    private String features; // comma-separated features or JSON string
}
