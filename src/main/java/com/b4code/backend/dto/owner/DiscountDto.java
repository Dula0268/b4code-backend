package com.b4code.backend.dto.owner;

import com.b4code.backend.models.Discount;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscountDto {
    private Long id;
    private Long propertyId;
    private String name;
    private String type;
    private String percentage;
    private Integer minNights;
    private Integer daysInAdvance;
    private String startDate;
    private String endDate;
    private Boolean isActive;

    public static DiscountDto fromEntity(Discount d) {
        return DiscountDto.builder()
                .id(d.getId())
                .propertyId(d.getProperty() != null ? d.getProperty().getId() : null)
                .name(d.getName())
                .type(d.getType())
                .percentage(d.getPercentage() != null ? d.getPercentage().toPlainString() : "0")
                .minNights(d.getMinNights())
                .daysInAdvance(d.getDaysInAdvance())
                .startDate(d.getStartDate() != null ? d.getStartDate().toString() : null)
                .endDate(d.getEndDate() != null ? d.getEndDate().toString() : null)
                .isActive(d.getIsActive())
                .build();
    }
}
