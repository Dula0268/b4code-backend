package com.hospitality.dto.admin;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevParDto {
    private Long propertyId;
    private String propertyName;
    private String type;
    private String roomNumber;
    private int adults;
    private int sqm;
    private String image;
    private BigDecimal revpar;
    private BigDecimal avgDailyRate;
    private double occupancyRate;
    private String currency;
}
