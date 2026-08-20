package com.b4code.backend.dto.owner;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RestrictionRequest {
    private Long propertyId;
    private String name;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean isActive;
}
