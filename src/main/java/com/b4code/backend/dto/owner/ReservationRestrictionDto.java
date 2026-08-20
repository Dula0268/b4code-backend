package com.b4code.backend.dto.owner;

import com.b4code.backend.models.ReservationRestriction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationRestrictionDto {
    private Long id;
    private Long propertyId;
    private String name;
    private String type;
    private String startDate;
    private String endDate;
    private String reason;
    private Boolean isActive;

    public static ReservationRestrictionDto fromEntity(ReservationRestriction r) {
        return ReservationRestrictionDto.builder()
                .id(r.getId())
                .propertyId(r.getProperty() != null ? r.getProperty().getId() : null)
                .name(r.getName())
                .type(r.getType())
                .startDate(r.getStartDate() != null ? r.getStartDate().toString() : null)
                .endDate(r.getEndDate() != null ? r.getEndDate().toString() : null)
                .reason(r.getReason())
                .isActive(r.getIsActive())
                .build();
    }
}
