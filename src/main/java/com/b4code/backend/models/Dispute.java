package com.b4code.backend.models;

import com.b4code.backend.models.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputes", schema = "admin")
@Getter
@Setter
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String disputeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private User guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_fk")
    private Booking booking;

    @Column(columnDefinition = "TEXT")
    private String reason;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3, columnDefinition = "TEXT")
    private String currency = "LKR";

    @Column(columnDefinition = "TEXT")
    private String stayDates;
    @Column(columnDefinition = "TEXT")
    private String cancellationPolicy;
    private Integer daysUntilAutoClose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status;

    @Column(columnDefinition = "TEXT")
    private String resolutionNote;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_admin_id")
    private User resolvedByAdmin;

    @Column(columnDefinition = "TEXT")
    private String internalNote;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime openedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
