package com.b4code.backend.models;

import com.b4code.backend.models.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_logs", schema = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    @Column(name = "changed_by")
    private String changedBy;

    /**
     * Type of actor behind {@link #changedBy}. {@code changedBy} alone is ambiguous - it holds
     * a guest display name for guest actions and a staff email for staff actions - so the
     * audit trail (and the "cancelled by ..." UI) needs this explicit discriminator.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "changed_by_role", length = 20)
    private com.b4code.backend.models.enums.OrderActorType changedByRole;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
