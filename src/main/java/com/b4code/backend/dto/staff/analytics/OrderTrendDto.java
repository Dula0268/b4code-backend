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
public class OrderTrendDto {
    private String timestamp;
    private Long count;
    private BigDecimal revenue;
}
