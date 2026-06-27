package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_prefs", schema = "owner",
       uniqueConstraints = @UniqueConstraint(columnNames = "owner_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean emailBooking = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean emailCancellation = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean emailReview = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean smsBooking = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean smsCancellation = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
