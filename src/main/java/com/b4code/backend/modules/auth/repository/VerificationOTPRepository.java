package com.b4code.backend.modules.auth.repository;

import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.entity.VerificationOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationOTPRepository extends JpaRepository<VerificationOTP, Long> {
    Optional<VerificationOTP> findByUser(User user);
    void deleteByUser(User user);
}
