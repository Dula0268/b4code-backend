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
    private java.math.BigDecimal pendingRefunds;  // same as totalRefunds, named for frontend
    private String currency;
}

