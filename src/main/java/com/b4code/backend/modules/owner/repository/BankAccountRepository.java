package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
