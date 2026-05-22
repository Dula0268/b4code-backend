package com.b4code.backend.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardKpiDto {
    private KpiValueDto totalRevenue;
    private KpiValueDto occupancyRate;
    private KpiValueDto activeBookings;
}

