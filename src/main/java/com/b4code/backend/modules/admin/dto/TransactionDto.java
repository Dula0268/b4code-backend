package com.b4code.backend.modules.admin.dto;

// Phase 3 — Finance: Transaction DTO

import com.b4code.backend.modules.admin.enums.TransactionType;
import com.b4code.backend.modules.admin.models.Transaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionDto {

    private Long id;
    private String referenceNumber;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private Long propertyId;
    private String propertyName;
    private Long userId;
    private String userName;
    private String description;
    private LocalDateTime createdAt;

    public static TransactionDto fromEntity(Transaction t) {
        return TransactionDto.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .type(t.getType())
                .propertyId(t.getPropertyId())
                .propertyName(t.getPropertyName())
                .userId(t.getUserId())
                .userName(t.getUserName())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
