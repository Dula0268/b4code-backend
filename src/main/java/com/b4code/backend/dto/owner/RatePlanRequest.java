package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RatePlanRequest {
    private Long propertyId;
    private String name;
    private String type;
    private BigDecimal basePrice;
    private Integer minNights;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
