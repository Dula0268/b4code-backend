package com.b4code.backend.dto;

import com.b4code.backend.models.enums.TransactionType;
import com.b4code.backend.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public TransactionDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static TransactionDto fromEntity(Transaction t) {
        TransactionDto dto = new TransactionDto();
        dto.id = t.getId();
        dto.referenceNumber = t.getReferenceNumber();
        dto.amount = t.getAmount();
        dto.currency = t.getCurrency();
        dto.type = t.getType();
        dto.propertyId = t.getProperty() != null ? t.getProperty().getId() : null;
        dto.propertyName = t.getProperty() != null ? t.getProperty().getName() : null;
        dto.userId = t.getUser() != null ? t.getUser().getId() : null;
        dto.userName = t.getUser() != null ? t.getUser().getFullName() : null;
        dto.description = t.getDescription();
        dto.createdAt = t.getCreatedAt();
        return dto;
    }
}
