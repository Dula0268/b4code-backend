package com.b4code.backend.modules.staff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.b4code.backend.modules.staff.qr.entity.QRCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
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

    @Column(name = "guest_id", nullable = false)
    private Long guestId;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(nullable = false)
    private String status; // NEW, PREPARING, DELIVERED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_code_id")
    private QRCode qrCode; // QR code scanned to create this order

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
