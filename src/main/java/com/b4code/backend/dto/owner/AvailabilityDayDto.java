package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailabilityDayDto {
    private Long roomId;
    private String roomName;
    private String date;
    private String status;
    private String customPrice;
    private String notes;
    private Long availabilityId;
}
