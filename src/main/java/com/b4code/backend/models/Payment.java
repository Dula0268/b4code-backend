package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments", schema = "admin")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_fk")
    private Booking booking;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency = "LKR";

    @Column(nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(unique = true)
    private String orderId;

    @Column(name = "food_order_id")
    private Long foodOrderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_fk")
    private Transaction transaction;

    @Column
    private String cardHolderName;

    @Column
    private String cardLastFour;

    @Column
    private String failureReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }
}
