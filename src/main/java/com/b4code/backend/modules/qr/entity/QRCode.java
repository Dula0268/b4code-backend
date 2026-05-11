package com.b4code.backend.modules.qr.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "unique_qr_id", nullable = false, unique = true)
    private String uniqueQrId;

    @Column(nullable = true, unique = true)
    private String qrCodeValue;
    
    @Column(nullable = true)
    private Long orderId;
    
    @Column(nullable = true)
    private Long propertyId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "room_number")
    private String roomNumber;
    
    @Column(nullable = true)
    private String status = "ACTIVE";

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String location;

    @Column(length = 100)
    private String type;
    
    @Column(columnDefinition = "TEXT")
    private String qrImageData;
    
    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = true)
    private LocalDateTime updatedAt;
    
    private LocalDateTime scannedAt;
    
    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instructionText;

    @Column(nullable = true)
    private Boolean showRoomNumber = false;

    @Column(nullable = true)
    private Boolean showLogo = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
