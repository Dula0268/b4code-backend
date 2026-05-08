package com.hospitality.dto.property;

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
    private String uniqueQrId;
    private Long orderId;
    private Long propertyId;
    private String status;
    private String name;
    private String location;
    private String type;
    private String qrImageData;
    private String qrImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime scannedAt;
    private String description;
    private String instructionText;
    private Boolean showRoomNumber;
    private Boolean showLogo;
}
