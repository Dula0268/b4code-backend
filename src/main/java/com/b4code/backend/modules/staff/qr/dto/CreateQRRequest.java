package com.b4code.backend.modules.staff.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQRRequest {

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotBlank(message = "QR code name is required")
    private String name;

    private String location;

    @NotNull(message = "QR type is required")
    private String type; // DINING_TABLE, ROOM, OUTDOOR, BAR

    private String description;

    private String instructionText;

    private Boolean showRoomNumber = false;

    private Boolean showLogo = true;
}
