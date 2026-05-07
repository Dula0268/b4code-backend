package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long propertyId;
    private Long roomId;
    private Long guestId;
    
    private String propertyName;
    private String roomName;
    private String guestName;
    private String guestEmail;
    private String guestTier;
    
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    
    private Double totalPrice;
    private String paymentStatus;
    private String status;
    
    private LocalDate createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }
}
