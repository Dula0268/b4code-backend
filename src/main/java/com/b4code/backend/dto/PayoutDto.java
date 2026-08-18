package com.b4code.backend.dto;

import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.models.Payout;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutDto {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private Long propertyId;
    private String propertyName;
    private BigDecimal amount;
    private BigDecimal hotelAmount;
    private BigDecimal foodAmount;
    private BigDecimal commissionAmount;
    private BigDecimal commissionRate;
    private String currency;
    private PayoutStatus status;
    private String bankReference;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    // Bank details (fetched dynamically by ownerId)
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String branchCode;
    private String bankDetails;

    // Legacy fields for frontend compatibility
    private String hostName;
    private String period;

    public static PayoutDto fromEntity(Payout p) {
        return PayoutDto.builder()
                .id(p.getId())
                .ownerId(p.getOwnerId())
                .ownerName(p.getOwnerName())
                .propertyId(p.getPropertyId())
                .propertyName(p.getPropertyName())
                .amount(p.getAmount())
                .hotelAmount(p.getHotelAmount())
                .foodAmount(p.getFoodAmount())
                .commissionAmount(p.getCommissionAmount())
                .commissionRate(p.getCommissionRate())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .bankReference(p.getBankReference())
                .requestedAt(p.getRequestedAt())
                .processedAt(p.getProcessedAt())
                .hostName(p.getOwnerName())
                .period(p.getRequestedAt() != null
                        ? p.getRequestedAt().getMonth().toString() + " " + p.getRequestedAt().getYear()
                        : "N/A")
                .build();
    }
}
