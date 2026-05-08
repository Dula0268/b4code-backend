package com.hospitality.dto.admin;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingChartPointDto {
    private String month;       
    private BigDecimal value;   
}
