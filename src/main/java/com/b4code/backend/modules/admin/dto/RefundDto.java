package com.b4code.backend.modules.admin.dto;

import com.b4code.backend.modules.admin.enums.RefundStatus;
import com.b4code.backend.modules.admin.models.Refund;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefundDto {

    private Long id;
    private Long transactionId;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private RefundStatus status;
    private String adminNote;
    private LocalDateTime requestedAt;

    public static RefundDto fromEntity(Refund r) {
        return RefundDto.builder()
                .id(r.getId())
                .transactionId(r.getTransactionId())
                .userId(r.getUserId())
                .userName(r.getUserName())
                .amount(r.getAmount())
                .currency(r.getCurrency())
                .reason(r.getReason())
                .status(r.getStatus())
                .adminNote(r.getAdminNote())
                .requestedAt(r.getRequestedAt())
                .build();
    }
}
