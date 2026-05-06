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
    
    @Column(nullable = false, unique = true)
    private String qrCodeValue;
    
    @Column(nullable = false)
    private Long orderId;
    
    @Column(nullable = false)
    private Long propertyId;
    
    @Column(nullable = false)
    private String status;
    
    @Column(columnDefinition = "LONGTEXT")
    private String qrImageData;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    private LocalDateTime scannedAt;
    
    @Column(length = 500)
    private String description;
}
