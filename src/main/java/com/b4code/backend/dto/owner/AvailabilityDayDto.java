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
    private Integer inventory;        // total units in this room type
    private Integer bookedQuantity;   // units booked on this date (from room_booking_daily)
    private Integer availableQuantity; // inventory - bookedQuantity
}
