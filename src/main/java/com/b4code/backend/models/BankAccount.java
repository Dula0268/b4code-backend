package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts", schema = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Column(nullable = false, length = 150)
    private String accountHolder;

    @Column(nullable = false, length = 50)
    private String accountNumber;

    @Column(length = 20)
    private String branchCode;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
