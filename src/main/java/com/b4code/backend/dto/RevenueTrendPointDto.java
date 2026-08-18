package com.b4code.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueTrendPointDto {
    private String month;
    private BigDecimal revenue;
    private BigDecimal netRevenue;
}
