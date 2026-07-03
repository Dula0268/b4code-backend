package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomPriceDto {
    private Long id;
    private String name;
    private String pricePerNight;
    private String status;
}
