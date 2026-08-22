package com.b4code.backend.service;

import com.b4code.backend.dao.GuestVerificationOTPRepository;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.GuestVerificationOTP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestOTPService {

    private final GuestVerificationOTPRepository guestVerificationOTPRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendGuestOTP(String email, String guestName) {
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        
        Optional<GuestVerificationOTP> existingOtpOpt = guestVerificationOTPRepository.findByEmail(email);
        GuestVerificationOTP guestOtp;
        if (existingOtpOpt.isPresent()) {
            guestOtp = existingOtpOpt.get();
            guestOtp.setOtp(otp);
            guestOtp.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(10));
        } else {
            guestOtp = new GuestVerificationOTP(email, otp, 10);
        }
        
        guestVerificationOTPRepository.save(guestOtp);
        
        // Send email
        emailService.sendVerificationOTPEmail(email, guestName, otp);
    }

    @Transactional
    public boolean verifyGuestOTP(String email, String otp) {
        Optional<GuestVerificationOTP> otpOpt = guestVerificationOTPRepository.findByEmail(email);
        
        if (otpOpt.isEmpty()) {
            throw new CustomException("OTP not found for this email");
        }
        
        GuestVerificationOTP guestOtp = otpOpt.get();
        
        if (guestOtp.isExpired()) {
            guestVerificationOTPRepository.delete(guestOtp);
            throw new CustomException("OTP has expired");
        }
        
        if (!guestOtp.getOtp().equals(otp)) {
            throw new CustomException("Invalid OTP");
        }
        
        // Success
        guestVerificationOTPRepository.delete(guestOtp);
        return true;
    }
}
