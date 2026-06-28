package com.b4code.backend.models;

import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.models.enums.FlagType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "flagged_reviews", schema = "admin")
@Getter
@Setter
public class FlaggedReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagType flagType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "property_id")
    private Long propertyId;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "flag_reason", columnDefinition = "TEXT")
    private String flagReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime flaggedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
