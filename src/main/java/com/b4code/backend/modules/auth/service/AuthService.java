package com.b4code.backend.modules.auth.service;

import com.b4code.backend.modules.admin.dao.AuditLogRepository;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.modules.admin.models.AuditLog;
import com.b4code.backend.common.security.JwtUtil;
import com.b4code.backend.modules.auth.dto.AuthResponse;
import com.b4code.backend.modules.auth.dto.LoginRequest;
import com.b4code.backend.modules.auth.dto.RegisterRequest;
import com.b4code.backend.modules.auth.dto.UserProfileDto;
import com.b4code.backend.modules.auth.entity.PasswordResetToken;
import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.PasswordResetTokenRepository;
import com.b4code.backend.modules.auth.repository.UserRepository;
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

        User.Role role;
        try {
            role = request.getRole() != null
                    ? User.Role.valueOf(request.getRole().toUpperCase())
                    : User.Role.GUEST;
        } catch (IllegalArgumentException e) {
            role = User.Role.GUEST;
        }
        user.setRole(role);
        
        if (role == User.Role.STAFF && request.getPropertyId() != null) {
            user.setPropertyId(request.getPropertyId());
            user.setStatus(User.UserStatus.PENDING);
        } else if (role == User.Role.GUEST || role == User.Role.OWNER) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        userRepository.save(user);

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
                user.getNationalIdUrl()
        );

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

        // ✅ Block login for specific statuses
        if (user.getStatus() == User.UserStatus.REJECTED) {
            throw new CustomException("Your account has been rejected. Please contact support.", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new CustomException("Your account has been suspended.", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == User.UserStatus.PENDING) {
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
                user.getNationalIdUrl()
        );

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
                .orElseThrow(() -> new CustomException("Invalid or expired password reset token", HttpStatus.BAD_REQUEST));

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
}