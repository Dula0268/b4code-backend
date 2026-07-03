package com.b4code.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PromoCodeRequest {
    private String code;
    private String description;
    private BigDecimal discountPercent;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer maxUses;
    private Long propertyId;
    private Long roomId;
}
