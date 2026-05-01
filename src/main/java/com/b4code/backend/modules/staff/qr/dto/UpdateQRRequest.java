package com.b4code.backend.modules.staff.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQRRequest {

    private String name;

    private String location;

    private String type;

    private String description;

    private String instructionText;

    private Boolean showRoomNumber;

    private Boolean showLogo;

    private String status; // ACTIVE, INACTIVE
}
