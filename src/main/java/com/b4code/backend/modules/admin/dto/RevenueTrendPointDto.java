package com.b4code.backend.modules.admin.dto;

// Phase 3 — Finance: revenue trend chart data point
// Frontend RevenueTrendChart uses dataKey="revenue" — NOT "value"

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevenueTrendPointDto {
    private String month;       // e.g. "Jan", "Feb"
    private BigDecimal revenue; // matches frontend dataKey="revenue" in RevenueTrendChart
}
