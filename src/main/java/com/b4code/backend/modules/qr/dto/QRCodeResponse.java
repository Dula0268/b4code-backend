package com.b4code.backend.modules.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCodeResponse {
    
    private Long id;
    private String qrCodeValue;
    private Long orderId;
    private Long propertyId;
    private String status;
    private String qrImageData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime scannedAt;
    private String description;
}
