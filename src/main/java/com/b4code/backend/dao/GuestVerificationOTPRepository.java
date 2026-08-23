package com.b4code.backend.dao;

import com.b4code.backend.models.GuestVerificationOTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestVerificationOTPRepository extends JpaRepository<GuestVerificationOTP, Long> {
    Optional<GuestVerificationOTP> findByEmail(String email);
    void deleteByEmail(String email);
}
