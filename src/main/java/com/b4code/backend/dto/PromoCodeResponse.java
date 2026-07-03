package com.b4code.backend.dto;

import com.b4code.backend.models.PromoCode;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PromoCodeResponse {
    private Long id;
    private String code;
    private String description;
    private BigDecimal discountPercent;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer maxUses;
    private Integer currentUses;
    private Boolean active;
    private Long propertyId;
    private String propertyName;
    private Long roomId;
    private String roomName;

    public static PromoCodeResponse from(PromoCode p, String propertyName, String roomName) {
        return PromoCodeResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .description(p.getDescription())
                .discountPercent(p.getDiscountPercent())
                .validFrom(p.getValidFrom())
                .validTo(p.getValidTo())
                .maxUses(p.getMaxUses())
                .currentUses(p.getCurrentUses())
                .active(p.getActive())
                .propertyId(p.getPropertyId())
                .propertyName(propertyName)
                .roomId(p.getRoomId())
                .roomName(roomName)
                .build();
    }
}
