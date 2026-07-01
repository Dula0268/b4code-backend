package com.b4code.backend.dto.staff.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long completedCount;
    private Long rejectedCount;
    private BigDecimal averageOrderValue;
}
