package com.b4code.backend.dto;

import com.b4code.backend.models.enums.TransactionType;
import com.b4code.backend.models.Transaction;
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
                .propertyId(t.getProperty() != null ? t.getProperty().getId() : null)
                .propertyName(t.getProperty() != null ? t.getProperty().getName() : null)
                .userId(t.getUser() != null ? t.getUser().getId() : null)
                .userName(t.getUser() != null ? t.getUser().getFullName() : null)
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

