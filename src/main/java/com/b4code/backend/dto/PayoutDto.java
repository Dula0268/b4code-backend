package com.b4code.backend.dto;

import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.models.Payout;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayoutDto {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private BigDecimal amount;
    private String currency;
    private PayoutStatus status;
    private String bankReference;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    public static PayoutDto fromEntity(Payout p) {
        return PayoutDto.builder()
                .id(p.getId())
                .ownerId(p.getOwnerId())
                .ownerName(p.getOwnerName())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .bankReference(p.getBankReference())
                .requestedAt(p.getRequestedAt())
                .processedAt(p.getProcessedAt())
                .build();
    }
}

