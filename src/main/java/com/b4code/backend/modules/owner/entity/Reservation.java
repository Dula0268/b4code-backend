package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    private String propertyName;

    @Column(name = "room_id")
    private Long roomId;

    private String roomName;

    @Column(name = "guest_id", nullable = false)
    private Long guestId;

    private String guestName;
    private String guestEmail;
    private String guestTier; // GOLD_MEMBER, NEW_GUEST, REGULAR_GUEST, BUSINESS_TRAVELER

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

    private String paymentStatus; // PAID, PARTIAL, UNPAID

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
