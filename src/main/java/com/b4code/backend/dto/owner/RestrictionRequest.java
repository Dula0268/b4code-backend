package com.b4code.backend.dto.owner;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RestrictionRequest {
    private Long propertyId;
    private Long roomTypeId;
    private String name;
    private String type;
    private Integer minStay;
    private Integer maxStay;
    private Boolean closedToArrival;
    private Boolean closedToDeparture;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean isActive;
}
