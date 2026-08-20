package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DiscountRequest {
    private Long propertyId;
    private String name;
    private String type;
    private BigDecimal percentage;
    private Integer minNights;
    private Integer daysInAdvance;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
