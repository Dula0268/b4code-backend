package com.b4code.backend.modules.guest.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Guest info (in a real system, link to User entity)
    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String guestEmail;

    @Column(nullable = false)
    private String guestPhone;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer guestCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    private String promoCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    private String confirmationNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = BookingStatus.CONFIRMED;
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    }

    public enum PaymentMethod {
        ONLINE_CARD, PAY_AT_PROPERTY
    }
}