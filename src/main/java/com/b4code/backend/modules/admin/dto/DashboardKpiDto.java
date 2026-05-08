package com.b4code.backend.modules.admin.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardKpiDto {
    private KpiValueDto totalRevenue;
    private KpiValueDto occupancyRate;
    private KpiValueDto activeBookings;
}
