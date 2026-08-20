package com.b4code.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusDto {
    private boolean checkedIn;
    private String roomNumber;
    private String roomTypeName;
    private String guestName;
    private String checkOutDate;
}
