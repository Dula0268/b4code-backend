package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owner_bank_details", schema = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerBankDetails {

    @Id
    @Column(name = "owner_id")
    private Long ownerId;

    @Column(length = 255)
    private String accountHolderName;

    @Column(length = 255)
    private String accountNumber;

    @Column(length = 255)
    private String bankName;

    @Column(length = 255)
    private String branchName;
}
