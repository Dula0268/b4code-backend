package com.b4code.backend.modules.staff.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRResponse {

    private Long id;

    private String uniqueQrId;

    private String name;

    private String location;

    private String type;

    private String status;

    private String description;

    private String instructionText;

    private Boolean showRoomNumber;

    private Boolean showLogo;

    private Long propertyId;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private Integer scans;

    private LocalDateTime lastScannedAt;

    private String qrImageUrl; // URL to fetch the QR image
}
