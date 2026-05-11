package com.b4code.backend.modules.guest.models;

import com.b4code.backend.modules.admin.enums.PropertyStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity(name = "GuestProperty")
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;

    @Column(nullable = false)
    private String address;

    private String destination;

    private String propertyType;   // Villa, Apartment, Guesthouse, Hotel

    private String badge;          // "Superhost", "Guest favorite", or null

    private Double latitude;
    private Double longitude;

    private String imageSrc;       // URL to primary property image

    @Column(length = 4000)
    private String galleryImages;  // comma-separated URLs for gallery

    @Column(length = 2000)
    private String description;

    // Host info
    private String hostName;

    @Column(length = 500)
    private String hostBio;
    private Integer hostYears;
    private Boolean hostSuperhost;

    // Guest capacity
    @Builder.Default
    private Integer baseGuests = 2;

    @Column(precision = 10, scale = 2)
    private BigDecimal extraGuestFee;

    // Ratings & reviews
    private Double averageRating;
    private Integer reviewCount;

    @Builder.Default
    private Boolean published = false;

    // Admin-required fields to satisfy DB constraints
    @Column(nullable = false)
    @Builder.Default
    private Long ownerId = 1L;

    @Column(nullable = false, unique = true)
    @Builder.Default
    private String pvId = java.util.UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.APPROVED;

    // Amenities stored as JSON-style string: "Wifi,Pool,Air conditioning"
    @Column(length = 1000)
    private String amenities;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = Room.class)
    private List<Room> rooms;
}