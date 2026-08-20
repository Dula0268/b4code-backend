package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import com.b4code.backend.models.enums.PropertyStatus;

import java.time.LocalDateTime;
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

    // Admin-focused Fields
    @Column(nullable = true, unique = true)
    private String pvId;

    @Column(nullable = true)
    private Long ownerId;

    private String ownerName;

    // Legacy address field (kept for admin seeder compatibility)
    @Column(nullable = true)
    private String address;

    // Location
    private String addressLine1;
    private String city;
    private String country;
    private String destination;

    private Double latitude;
    private Double longitude;

    // Property type & display
    private String propertyType; // Villa, Apartment, Guesthouse, Hotel
    private String badge;        // "Superhost", "Guest favorite"

    // Image & Gallery (legacy admin fields)
    private String imageUrl;
    private String imageSrc;
    @Column(length = 4000)
    private String galleryImages;

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

    // Ratings & Reviews
    private Double averageRating;
    private Integer reviewCount;
    
    private Double avgCleanliness;
    private Double avgComfort;
    private Double avgService;
    private Double avgDining;
    private Double avgLocation;
    private Double avgValue;

    // Contact
    @Column(length = 30)
    private String contactPhone;

    @Column(length = 120)
    private String contactEmail;

    // Policies
    @Column(length = 50)
    @Builder.Default
    private String checkInTime = "14:00";

    @Column(length = 50)
    @Builder.Default
    private String checkOutTime = "11:00";

    @Column(length = 1000)
    @Builder.Default
    private String cancellationPolicy = "Free cancellation until 48 hours before.\n50% refund within 48 hours.\nNo-shows will be charged full amount.";

    @Column(length = 500)
    @Builder.Default
    private String childPolicy = "Children of any age are welcome.\nNo age restriction for check-in.\nExtra beds available upon request.";

    @Column(length = 1000)
    @Builder.Default
    private String houseRules = "No smoking indoors.\nNo pets allowed.\nNo parties or events.";

    @Column(name = "service_charge_rate")
    @Builder.Default
    private Double serviceChargeRate = 10.0;

    // Admin & Reviews
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.PENDING;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new java.util.ArrayList<>();

    // Relationships
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Amenity> amenities = new java.util.HashSet<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<RoomType> roomTypes = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Image> images = new java.util.ArrayList<>();
}
