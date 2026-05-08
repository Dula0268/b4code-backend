package com.hospitality.models;

import com.hospitality.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "flagged_reviews")
@Getter
@Setter
public class FlaggedReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long propertyId;
    @Column(columnDefinition = "TEXT")
    private String propertyName;

    private Long guestId;
    @Column(columnDefinition = "TEXT")
    private String guestName;
    private String guestInitial;
    private String guestAvatarColor;

    @Column(columnDefinition = "TEXT")
    private String reviewText;

    private Double rating;           

    @Column(columnDefinition = "TEXT")
    private String flagReason;       

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime flaggedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
