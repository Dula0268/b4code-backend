package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayoutRequestDto {
    private Long propertyId;
    private BigDecimal amount;
}
