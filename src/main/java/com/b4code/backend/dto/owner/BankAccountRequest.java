package com.b4code.backend.dto.owner;

import lombok.Data;

@Data
public class BankAccountRequest {
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String branchCode;
    private Boolean isPrimary;
}
