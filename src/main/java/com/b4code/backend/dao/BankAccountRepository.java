package com.b4code.backend.dao;

import com.b4code.backend.models.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByOwnerIdOrderByIsPrimaryDescCreatedAtDesc(Long ownerId);
}
