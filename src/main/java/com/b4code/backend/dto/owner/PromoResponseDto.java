package com.b4code.backend.dto.owner;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoResponseDto {

    private Long id;
    private String code;
    private String description;
    private BigDecimal discountPercent;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer maxUses;
    private Integer currentUses;
    private Boolean active;
    private Boolean isValid;
    private Long propertyId;
    private String propertyName;
}
