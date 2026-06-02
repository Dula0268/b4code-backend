package com.b4code.backend.service;

import com.b4code.backend.dao.AuditLogRepository;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.AuditLog;
import com.b4code.backend.common.security.JwtUtil;
import com.b4code.backend.dto.AuthResponse;
import com.b4code.backend.dto.LoginRequest;
import com.b4code.backend.dto.RegisterRequest;
import com.b4code.backend.dto.UserProfileDto;
import com.b4code.backend.models.PasswordResetToken;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.dao.PasswordResetTokenRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dao.VerificationOTPRepository;
import com.b4code.backend.models.VerificationOTP;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogRepository auditLogRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final VerificationOTPRepository verificationOTPRepository;

    @Value("${app.frontend-url:http://localhost:3001}")
    private String frontendUrl;

    // ───────────────────────── REGISTER ─────────────────────────
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName() != null ? request.getLastName() : "");
        } else if (request.getPhone() != null) {
            String[] parts = request.getPhone().split(" ", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
        }

        user.setPhone(request.getPhone());

        UserRole role;
        try {
            role = request.getRole() != null
                    ? UserRole.valueOf(request.getRole().toUpperCase())
                    : UserRole.GUEST;
        } catch (IllegalArgumentException e) {
            role = UserRole.GUEST;
        }
        user.setRole(role);

        if (role == UserRole.STAFF && request.getPropertyId() != null) {
            user.setPropertyId(request.getPropertyId());
        }

        // Everyone starts as PENDING until email is verified via OTP
        user.setStatus(UserStatus.PENDING);

        userRepository.save(user);

        // ── Generate 6-digit OTP ──
        String otpCode = String.format("%06d", new java.util.Random().nextInt(999999));

        // Remove any old OTP for this user if it exists
        verificationOTPRepository.findByUser(user).ifPresent(verificationOTPRepository::delete);

        VerificationOTP otp = new VerificationOTP(otpCode, user, 10); // 10 minutes expiry
        verificationOTPRepository.save(otp);

        // ── Send Verification Email ──
        emailService.sendVerificationOTPEmail(user.getEmail(), user.getFirstName(), otpCode);

        AuditLog log = new AuditLog();
        log.setUserId(user.getId());
        log.setUserName(user.getEmail());
        log.setUserRole(user.getRole().name());
        log.setAction("REGISTER");
        log.setEntity("AUTH");
        log.setEntityDetail(user.getEmail());
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        UserProfileDto profile = new UserProfileDto(
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getNationalIdUrl());

        return new AuthResponse(token, refreshToken, user.getEmail(),
                user.getRole().name(), user.getId(), user.getStatus().name(), user.getPropertyId(), profile);
    }

    // ───────────────────────── LOGIN ─────────────────────────
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new CustomException("Email not found", HttpStatus.NOT_FOUND));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash());

        if (!passwordMatches) {

            AuditLog log = new AuditLog();
            log.setUserId(user.getId());
            log.setUserName(user.getEmail());
            log.setUserRole(user.getRole().name());
            log.setAction("LOGIN_FAILED");
            log.setEntity("AUTH");
            log.setEntityDetail("INCORRECT_PASSWORD");
            log.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(log);

            throw new CustomException("Incorrect password", HttpStatus.UNAUTHORIZED);
        }

        // Block login for specific statuses
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new CustomException("Your account has been rejected. Please contact support.", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new CustomException("Your account has been suspended.", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == UserStatus.PENDING) {
            throw new CustomException("Your account is still pending approval.", HttpStatus.FORBIDDEN);
        }

        AuditLog log = new AuditLog();
        log.setUserId(user.getId());
        log.setUserName(user.getEmail());
        log.setUserRole(user.getRole().name());
        log.setAction("LOGIN_SUCCESS");
        log.setEntity("AUTH");
        log.setEntityDetail(user.getEmail());
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        UserProfileDto profile = new UserProfileDto(
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getNationalIdUrl());

        return new AuthResponse(
                token,
                refreshToken,
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                user.getStatus().name(),
                user.getPropertyId(),
                profile);
    }

    // ───────────────────────── FORGOT PASSWORD ─────────────────────────
    @Transactional
    public String forgotPassword(String email) {

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // Remove old token first
        passwordResetTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.flush();

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                30 // 30 minutes expiry
        );

        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        return token;
    }

    // ───────────────────────── RESET PASSWORD ─────────────────────────
    @Transactional
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(
                        () -> new CustomException("Invalid or expired password reset token", HttpStatus.BAD_REQUEST));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new CustomException("Password reset token has expired", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Clear the token after successful reset
        passwordResetTokenRepository.delete(resetToken);

        // Log the action
        AuditLog log = new AuditLog();
        log.setUserId(user.getId());
        log.setUserName(user.getEmail());
        log.setUserRole(user.getRole().name());
        log.setAction("PASSWORD_RESET");
        log.setEntity("AUTH");
        log.setEntityDetail(user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    // ───────────────────────── VERIFY EMAIL (OTP) ─────────────────────────
    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        VerificationOTP otp = verificationOTPRepository.findByUser(user)
                .orElseThrow(
                        () -> new CustomException("No verification code found for this user", HttpStatus.BAD_REQUEST));

        if (otp.isExpired()) {
            verificationOTPRepository.delete(otp);
            throw new CustomException("Verification code has expired", HttpStatus.BAD_REQUEST);
        }

        if (!otp.getOtp().equals(code)) {
            throw new CustomException("Invalid verification code", HttpStatus.BAD_REQUEST);
        }

        // Logic for role-based activation
        if (user.getRole() == UserRole.STAFF) {
            // Staff members verified their email, but still need Owner approval
            // So we keep them as PENDING, but we can mark their email as verified if we had
            // a flag.
            // For now, they stay PENDING so they can't login yet.
            user.setStatus(UserStatus.PENDING);
        } else {
            // Guests and Owners are activated immediately after OTP verification
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);
        verificationOTPRepository.delete(otp); // Clear OTP after success

        // Log the action
        AuditLog log = new AuditLog();
        log.setUserId(user.getId());
        log.setUserName(user.getEmail());
        log.setUserRole(user.getRole().name());
        log.setAction("EMAIL_VERIFIED");
        log.setEntity("AUTH");
        log.setEntityDetail(user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
