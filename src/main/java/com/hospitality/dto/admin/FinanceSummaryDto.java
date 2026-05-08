package com.hospitality.dto.admin;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinanceSummaryDto {
    private BigDecimal totalRevenue;
    private BigDecimal platformCommission;
    private BigDecimal totalPayouts;
    private BigDecimal totalRefunds;
    private Long pendingPayouts;
    private String currency;
}
