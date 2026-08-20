package com.b4code.backend.models;

import com.b4code.backend.models.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts", schema = "owner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;
    private String ownerName;

    private Long propertyId;
    private String propertyName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal hotelAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal foodAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "LKR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status;

    private String bankReference;
    
    @Column(length = 1000)
    private String adminNote;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime requestedAt;

    @UpdateTimestamp
    private LocalDateTime processedAt;
}
