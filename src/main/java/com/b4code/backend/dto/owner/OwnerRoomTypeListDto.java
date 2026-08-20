package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OwnerRoomTypeListDto {
    private List<OwnerRoomTypeDto> roomTypes;
    private long totalRoomTypes;
    private long occupied;
    private long maintenance;
    private long vacant;
}
