package com.b4code.backend.modules.admin.models;

import com.b4code.backend.modules.admin.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
@Getter
@Setter
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String disputeId;          

    private Long guestId;
    private String guestName;          

    private Long propertyId;
    private String propertyName;       

    private String bookingId;          

    private String reason;             
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "LKR";

    private String stayDates;          
    private String cancellationPolicy;
    private Integer daysUntilAutoClose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status;

    private String resolutionNote;
    private Long resolvedByAdminId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime openedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
