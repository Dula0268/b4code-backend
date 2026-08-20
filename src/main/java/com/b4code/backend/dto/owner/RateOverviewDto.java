package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RateOverviewDto {
    private Long propertyId;
    private List<RatePlanDto> ratePlans;
    private List<DiscountDto> discounts;
}
