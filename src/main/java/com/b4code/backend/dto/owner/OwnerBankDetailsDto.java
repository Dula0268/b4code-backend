package com.b4code.backend.dto.owner;

import com.b4code.backend.models.OwnerBankDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerBankDetailsDto {
    private Long ownerId;
    private String accountHolderName;
    private String accountNumber;
    private String bankName;
    private String branchName;

    public static OwnerBankDetailsDto fromEntity(OwnerBankDetails d) {
        return OwnerBankDetailsDto.builder()
                .ownerId(d.getOwnerId())
                .accountHolderName(d.getAccountHolderName())
                .accountNumber(d.getAccountNumber())
                .bankName(d.getBankName())
                .branchName(d.getBranchName())
                .build();
    }
}
