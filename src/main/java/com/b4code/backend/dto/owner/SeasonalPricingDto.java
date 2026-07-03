package com.b4code.backend.dto.owner;

import com.b4code.backend.models.SeasonalPricing;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@Builder
public class SeasonalPricingDto {
    private Long id;
    private Long propertyId;
    private String name;
    private String startDate;
    private String endDate;
    private String percentageAdjustment;
    private String dateRange;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public static SeasonalPricingDto fromEntity(SeasonalPricing s) {
        String pct = s.getPercentageAdjustment() != null
                ? (s.getPercentageAdjustment().compareTo(java.math.BigDecimal.ZERO) >= 0 ? "+" : "")
                  + s.getPercentageAdjustment().stripTrailingZeros().toPlainString() + "%"
                : "0%";
        String range = s.getStartDate() != null && s.getEndDate() != null
                ? s.getStartDate().format(FMT) + " – " + s.getEndDate().format(FMT)
                : "—";
        return SeasonalPricingDto.builder()
                .id(s.getId())
                .propertyId(s.getProperty() != null ? s.getProperty().getId() : null)
                .name(s.getName())
                .startDate(s.getStartDate() != null ? s.getStartDate().toString() : null)
                .endDate(s.getEndDate() != null ? s.getEndDate().toString() : null)
                .percentageAdjustment(pct)
                .dateRange(range)
                .build();
    }
}
