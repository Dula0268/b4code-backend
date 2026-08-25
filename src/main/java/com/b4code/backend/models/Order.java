package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "orders", schema = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "guest_id", nullable = true)
    private Long guestId;

    @Column(name = "guest_session_id", length = 36)
    private String guestSessionId;

    @Column(name = "location")
    private String location;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    /**
     * Authoritative money breakdown, computed and persisted server-side at order
     * creation time. These are the ONLY numbers any client (guest or staff) may
     * display — neither side is allowed to re-derive them from totalAmount.
     */
    @Column(name = "subtotal_amount")
    private Double subtotalAmount;

    @Column(name = "service_charge_amount")
    private Double serviceChargeAmount;

    @Column(name = "tax_amount")
    private Double taxAmount;

    @Column(name = "discount_amount")
    private Double discountAmount;

    /** Rate (percent) actually used to compute serviceChargeAmount, kept for display/audit. */
    @Column(name = "service_charge_rate")
    private Double serviceChargeRate;

    /** Rate (percent) actually used to compute taxAmount, kept for display/audit. */
    @Column(name = "tax_rate")
    private Double taxRate;

    /** Grand total = subtotal + serviceCharge + tax - discount. */
    @Column(name = "total_amount")
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.b4code.backend.models.enums.OrderStatus status;

    @Column(name = "guest_instructions", columnDefinition = "TEXT")
    private String guestInstructions;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "staff_notes", columnDefinition = "TEXT")
    private String staffNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();

    /**
     * Who cancelled this order (GUEST vs STAFF). Null while the order is not cancelled.
     * Drives the "Cancelled by guest" / "Cancelled by restaurant" wording on the guest
     * and staff screens - previously the screens had no way to tell the two apart.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by", length = 20)
    private com.b4code.backend.models.enums.OrderActorType cancelledBy;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    /**
     * Outcome of the refund attempt made when the order was cancelled. Persisted so both
     * staff and guest can see whether money actually went back, and so a second cancel
     * attempt can be short-circuited instead of refunding twice.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", length = 20)
    private com.b4code.backend.models.enums.OrderRefundStatus refundStatus;

    @Column(name = "refund_amount")
    private Double refundAmount;

    /** Payment provider reference (PayHere order id) the refund was issued against. */
    @Column(name = "refund_reference", length = 100)
    private String refundReference;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
