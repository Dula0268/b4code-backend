package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OwnerRoomRequest {
    private Long propertyId;
    private String name;
    private String description;
    private String roomType;
    private Integer maxOccupancy;
    private Integer maxChildren;
    private String bedType;
    private BigDecimal pricePerNight;
    private Integer inventory;
    private String status;
    private String imageUrl;
}
