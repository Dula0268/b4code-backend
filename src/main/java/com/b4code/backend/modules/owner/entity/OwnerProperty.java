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
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String pvId;

    private String imageUrl;

    @Column(nullable = false)
    private Long ownerId;

    private String ownerName;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, MAINTENANCE, PENDING, APPROVED, REJECTED

    private String rejectionReason;

    // ── New fields for frontend property CRUD ──

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;
    private String city;
    private String postalCode;
    private String propertyType; // Villa, Hotel, Apartment, Resort, etc.
    private String contact;
    private String email;
    private String checkInTime;
    private String checkOutTime;

    @Column(columnDefinition = "TEXT")
    private String rules;

    private String amenities; // Comma-separated: wifi,pool,parking,gym,...
    private String yearBuilt;

    @Column(precision = 10, scale = 2)
    private BigDecimal rate;

    private Double rating;
    private Integer reviewCount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
