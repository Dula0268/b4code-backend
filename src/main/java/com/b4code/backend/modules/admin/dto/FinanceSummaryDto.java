package com.b4code.backend.modules.admin.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinanceSummaryDto {
    private BigDecimal totalRevenue;
    private BigDecimal platformCommission;
    private BigDecimal totalPayouts;
    private BigDecimal totalRefunds;
    private String currency;
}
