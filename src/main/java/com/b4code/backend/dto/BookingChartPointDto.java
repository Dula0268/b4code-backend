package com.b4code.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingChartPointDto {
    private String month;       
    private BigDecimal value;   
    private BigDecimal netRevenue;
}

