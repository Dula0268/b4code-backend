package com.b4code.backend.modules.admin.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevParDto {
    private Long propertyId;
    private String propertyName;
    private BigDecimal revpar;
    private BigDecimal avgDailyRate;
    private double occupancyRate;
    private String currency;
}
