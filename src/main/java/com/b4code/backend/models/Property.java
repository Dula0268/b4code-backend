package com.b4code.backend.models;

import com.b4code.backend.models.enums.PropertyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    // Basic Info (required)
    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true, unique = true)
    private String pvId;

    // Admin-focused Fields
    @Column(nullable = true)
    private Long ownerId;

    private String ownerName;

    // Search & Display Fields
    private String city;
    private String destination;
    private String propertyType; // Villa, Apartment, Guesthouse, Hotel
    private String badge; // "Superhost", "Guest favorite"

    // Location coordinates for mapping
    private Double latitude;
    private Double longitude;

    // Image & Gallery
    private String imageUrl; // Primary image from admin
    private String imageSrc; // Primary image from guest view
    @Column(length = 4000)
    private String galleryImages; // comma-separated URLs for gallery

    // Description & Details
    @Column(length = 2000)
    private String description;

    // Host Information
    private String hostName;
    @Column(length = 500)
    private String hostBio;
    private Integer hostYears;
    private Boolean hostSuperhost;

    // Guest Capacity & Pricing
    @Builder.Default
    private Integer baseGuests = 2;
    @Column(precision = 10, scale = 2)
    private BigDecimal extraGuestFee;

    // Amenities (JSON-style string: "Wifi,Pool,Air conditioning")
    @Column(length = 1000)
    private String amenities;

    // Accessibility features (comma-separated or JSON-style string)
    @Column(length = 1000, nullable = true)
    private String accessibility;

    // Ratings & Reviews
    private Double averageRating;
    private Integer reviewCount;

    // Publishing Status
    @Builder.Default
    private Boolean published = false;

    // Admin Verification Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.APPROVED;

    // Admin Rejection Details
    private String rejectionReason;

    // Timestamps
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Room> rooms;
}
