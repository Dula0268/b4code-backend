package com.b4code.backend.modules.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeGenerateRequest {

    private Long orderId;
    private Long propertyId;
    private String name;
    private String location;
    private String type;
    private String description;
    private String instructionText;
    private Boolean showRoomNumber;
    private Boolean showLogo;
    private Long tableId;
    private String roomNumber;
}
