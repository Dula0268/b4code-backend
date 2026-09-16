package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_restrictions", schema = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 30)
    private String type;

    @Column(name = "min_stay")
    private Integer minStay;

    @Column(name = "max_stay")
    private Integer maxStay;

    @Column(name = "closed_to_arrival", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean closedToArrival = false;

    @Column(name = "closed_to_departure", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean closedToDeparture = false;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
