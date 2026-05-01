package com.b4code.backend.modules.staff.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRValidationResponse {

    private String qrId; // Unique QR ID

    private String propertyName; // Name of the property

    private String locationLabel; // Location/Table name or Room number

    private String type; // QR type (DINING_TABLE, ROOM, etc.)

    private String name; // Full name of the QR context

    private String status; // ACTIVE or INACTIVE

    private Boolean isValid; // Whether the QR code is valid and scannable
}
