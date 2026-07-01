package com.b4code.backend.dao;

import com.b4code.backend.models.OwnerBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerBankDetailsRepository extends JpaRepository<OwnerBankDetails, Long> {

    Optional<OwnerBankDetails> findByOwnerId(Long ownerId);
}
