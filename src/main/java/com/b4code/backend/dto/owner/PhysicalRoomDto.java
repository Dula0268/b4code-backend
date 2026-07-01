package com.b4code.backend.dto.owner;

import com.b4code.backend.models.PhysicalRoom;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhysicalRoomDto {
    private Long id;
    private Long roomId;
    private String roomName;
    private String doorNumber;
    private String status;

    public static PhysicalRoomDto fromEntity(PhysicalRoom pr) {
        return PhysicalRoomDto.builder()
                .id(pr.getId())
                .roomId(pr.getRoom() != null ? pr.getRoom().getId() : null)
                .roomName(pr.getRoom() != null ? pr.getRoom().getName() : null)
                .doorNumber(pr.getDoorNumber())
                .status(pr.getStatus())
                .build();
    }
}
