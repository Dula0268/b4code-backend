package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RoomInventoryLockDto {
    private Long roomTypeId;
    private String roomTypeName;
    private Integer configuredInventory;
    private Integer physicalRoomCount;
    private List<String> doorNumbers;
    private Integer activeBookingsCount;
    private Integer availableCount;
    private Boolean isOverbookingLocked;
}
