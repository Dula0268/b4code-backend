package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "table_number")
    private String tableNumber;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
