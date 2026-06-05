package com.b4code.backend.models;

import com.b4code.backend.models.enums.ReviewStatus;
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
    private Review review;

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
