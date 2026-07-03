package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SeasonalPricingRequest {
    private Long propertyId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal percentageAdjustment;
}
