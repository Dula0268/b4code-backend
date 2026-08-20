package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "availability", schema = "owner",
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 20, nullable = false, columnDefinition = "varchar(20) default 'AVAILABLE'")
    @Builder.Default
    private String status = "AVAILABLE";

    @Column(precision = 10, scale = 2)
    private BigDecimal customPrice;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
