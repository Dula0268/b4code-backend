package com.b4code.backend.dto.owner;

import com.b4code.backend.models.BankAccount;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountDto {
    private Long id;
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String branchCode;
    private Boolean isPrimary;

    public static BankAccountDto fromEntity(BankAccount b) {
        String masked = b.getAccountNumber() != null && b.getAccountNumber().length() > 4
                ? "****" + b.getAccountNumber().substring(b.getAccountNumber().length() - 4)
                : b.getAccountNumber();
        return BankAccountDto.builder()
                .id(b.getId())
                .bankName(b.getBankName())
                .accountHolder(b.getAccountHolder())
                .accountNumber(masked)
                .branchCode(b.getBranchCode())
                .isPrimary(b.getIsPrimary())
                .build();
    }
}
