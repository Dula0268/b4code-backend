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
public class TopMenuItemDto {
    private Long menuItemId;
    private String name;
    private Long volume;
    private BigDecimal revenue;
}
