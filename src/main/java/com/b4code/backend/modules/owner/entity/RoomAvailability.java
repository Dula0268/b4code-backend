package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "room_availability", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status; // AVAILABLE, BOOKED, BLOCKED

    private String guestName; // populated when status is BOOKED

    @Column(precision = 10, scale = 2)
    private BigDecimal customPrice;

    private String notes;
}
