package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties", schema = "owner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Info
    @Column(nullable = false)
    private String name;



    @Column(length = 2000)
    private String description;


    // Filters
    @Column(name = "free_cancellation", nullable = false)
    @Builder.Default
    private Boolean freeCancellation = false;

    @Column(name = "breakfast_included", nullable = false)
    @Builder.Default
    private Boolean breakfastIncluded = false;

    @Column(name = "pet_friendly", nullable = false)
    @Builder.Default
    private Boolean petFriendly = false;

    @Column(name = "accessibility", nullable = false)
    @Builder.Default
    private Boolean accessibility = false;

    // Location
    private String addressLine1;
    private String city;
    private String country;

    private Double latitude;
    private Double longitude;

    // Admin & Reviews
    @Column(nullable = false)
    private Long ownerId;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new java.util.ArrayList<>();

    // Relationships
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Amenity> amenities = new java.util.HashSet<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Image> images = new java.util.ArrayList<>();
}
