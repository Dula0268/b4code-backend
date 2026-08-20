package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OwnerRoomListDto {
    private List<OwnerRoomDto> rooms;
    private long totalRooms;
    private long occupied;
    private long maintenance;
    private long vacant;
}
