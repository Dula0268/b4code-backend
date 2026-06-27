package com.b4code.backend.dto;

import lombok.Data;

@Data
public class RoomLoginRequest {
    private String lastName;
    private String roomNumber;
    private Long propertyId;
}
