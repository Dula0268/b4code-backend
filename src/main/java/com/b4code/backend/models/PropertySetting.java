package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_settings", schema = "owner",
       uniqueConstraints = @UniqueConstraint(columnNames = "property_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false, columnDefinition = "integer default 1")
    @Builder.Default
    private Integer minStay = 1;

    @Column(nullable = false, columnDefinition = "integer default 30")
    @Builder.Default
    private Integer maxStay = 30;

    @Column(nullable = false, columnDefinition = "integer default 365")
    @Builder.Default
    private Integer advanceBookingDays = 365;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean instantBooking = true;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer bufferDays = 0;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
