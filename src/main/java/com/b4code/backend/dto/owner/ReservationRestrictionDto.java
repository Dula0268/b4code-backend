package com.b4code.backend.dto.owner;

import com.b4code.backend.models.ReservationRestriction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationRestrictionDto {
    private Long id;
    private Long propertyId;
    private Long roomTypeId;
    private String roomTypeName;
    private String name;
    private String type;
    private Integer minStay;
    private Integer maxStay;
    private Boolean closedToArrival;
    private Boolean closedToDeparture;
    private String startDate;
    private String endDate;
    private String reason;
    private Boolean isActive;

    public static ReservationRestrictionDto fromEntity(ReservationRestriction r) {
        return ReservationRestrictionDto.builder()
                .id(r.getId())
                .propertyId(r.getProperty() != null ? r.getProperty().getId() : null)
                .roomTypeId(r.getRoomType() != null ? r.getRoomType().getId() : null)
                .roomTypeName(r.getRoomType() != null ? r.getRoomType().getName() : "All Rooms")
                .name(r.getName())
                .type(r.getType())
                .minStay(r.getMinStay())
                .maxStay(r.getMaxStay())
                .closedToArrival(r.getClosedToArrival())
                .closedToDeparture(r.getClosedToDeparture())
                .startDate(r.getStartDate() != null ? r.getStartDate().toString() : null)
                .endDate(r.getEndDate() != null ? r.getEndDate().toString() : null)
                .reason(r.getReason())
                .isActive(r.getIsActive())
                .build();
    }
}
