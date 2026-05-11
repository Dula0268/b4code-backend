package com.b4code.backend.modules.admin.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinanceSummaryDto {
    private java.math.BigDecimal totalRevenue;
    private java.math.BigDecimal platformCommission;
    private java.math.BigDecimal totalPayouts;
    private java.math.BigDecimal totalRefunds;
    private java.math.BigDecimal pendingRefunds;  // same as totalRefunds, named for frontend
    private String currency;
}
