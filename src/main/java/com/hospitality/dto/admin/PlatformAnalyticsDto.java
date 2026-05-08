package com.hospitality.dto.admin;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlatformAnalyticsDto {
    private BigDecimal grossBookingValue;
    private double grossBookingValueChangePct;
    private BigDecimal netRevenue;
    private int commissionRate;         
    private double occupancyRate;       
    private BigDecimal avgDailyRate;
    private BigDecimal avgDailyRateGoal;
    private BigDecimal revpar;
    private String currency;
}
