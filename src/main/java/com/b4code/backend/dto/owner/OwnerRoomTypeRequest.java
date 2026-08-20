package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OwnerRoomTypeRequest {
    private Long propertyId;
    private String name;
    private String description;
    private String roomCategory;
    private Integer maxOccupancy;
    private Integer maxChildren;
    private String bedType;
    private BigDecimal pricePerNight;
    private Integer inventory;
    private String status;
}
