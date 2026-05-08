package com.b4code.backend.modules.guest.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private Integer overallRating;      // 1-5 stars

    private Integer cleanlinessRating;
    private Integer accuracyRating;
    private Integer communicationRating;
    private Integer locationRating;
    private Integer valueRating;

    @Column(length = 2000)
    private String comment;

    private String photoUrls;  // comma-separated URLs

    @Column(nullable = false)
    private Boolean isVerifiedStay = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Owner response
    private String ownerResponse;
    private LocalDateTime ownerRespondedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
