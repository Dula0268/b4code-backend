package com.b4code.backend.modules.admin.models;

import com.b4code.backend.modules.admin.enums.ReviewStatus;
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
    private String propertyName;

    private Long guestId;
    private String guestName;
    private String guestInitial;
    private String guestAvatarColor;

    @Column(columnDefinition = "TEXT")
    private String reviewText;

    private Double rating;           

    private String flagReason;       

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    private String adminNote;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime flaggedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
