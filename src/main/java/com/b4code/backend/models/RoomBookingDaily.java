package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "room_booking_daily", schema = "owner",
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomBookingDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer bookedQuantity = 0;
}
