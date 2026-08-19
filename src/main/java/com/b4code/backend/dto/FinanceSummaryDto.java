package com.b4code.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinanceSummaryDto {
    private java.math.BigDecimal totalRevenue;
    private String revenueGrowth;
    private java.math.BigDecimal platformCommission;
    private String commissionGrowth;
    private java.math.BigDecimal totalPayouts;
    private String payoutGrowth;
    private java.math.BigDecimal totalRefunds;
    private String refundsGrowth;
    private Long pendingRefunds;
    private Long allPayoutsCount;
    private Long pendingPayouts;
    private String currency;
}

