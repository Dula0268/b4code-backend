package com.b4code.backend.dto.owner;

import com.b4code.backend.models.RatePlan;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatePlanDto {
    private Long id;
    private Long propertyId;
    private String name;
    private String type;
    private String basePrice;
    private Integer minNights;
    private String startDate;
    private String endDate;
    private Boolean isActive;

    public static RatePlanDto fromEntity(RatePlan r) {
        return RatePlanDto.builder()
                .id(r.getId())
                .propertyId(r.getProperty() != null ? r.getProperty().getId() : null)
                .name(r.getName())
                .type(r.getType())
                .basePrice(r.getBasePrice() != null ? r.getBasePrice().toPlainString() : "0")
                .minNights(r.getMinNights())
                .startDate(r.getStartDate() != null ? r.getStartDate().toString() : null)
                .endDate(r.getEndDate() != null ? r.getEndDate().toString() : null)
                .isActive(r.getIsActive())
                .build();
    }
}
