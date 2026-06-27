package com.b4code.backend.dto.owner;

import com.b4code.backend.models.PropertySetting;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertySettingDto {
    private Long propertyId;
    private Integer minStay;
    private Integer maxStay;
    private Integer advanceBookingDays;
    private Boolean instantBooking;
    private Integer bufferDays;

    public static PropertySettingDto fromEntity(PropertySetting s) {
        return PropertySettingDto.builder()
                .propertyId(s.getPropertyId())
                .minStay(s.getMinStay())
                .maxStay(s.getMaxStay())
                .advanceBookingDays(s.getAdvanceBookingDays())
                .instantBooking(s.getInstantBooking())
                .bufferDays(s.getBufferDays())
                .build();
    }

    public static PropertySettingDto defaults(Long propertyId) {
        return PropertySettingDto.builder()
                .propertyId(propertyId).minStay(1).maxStay(30)
                .advanceBookingDays(365).instantBooking(true).bufferDays(0)
                .build();
    }
}
