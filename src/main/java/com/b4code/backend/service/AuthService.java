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
    private final com.b4code.backend.dao.BookingRepository bookingRepository;

    @Value("${app.frontend-url:http://localhost:3001}")
    private String frontendUrl;

    // ───────────────────────── REGISTER ─────────────────────────
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("This email is already registered. Please log in instead.", HttpStatus.CONFLICT);
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
            if (request.getStaffRole() != null) {
                user.setStaffRole(request.getStaffRole());
            }
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
        log.setUser(user);
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
                user.getNationalIdUrl(),
                user.getStaffRole());

        return new AuthResponse(token, refreshToken, user.getEmail(),
                user.getRole().name(), user.getId(), user.getStatus().name(), user.getPropertyId(), profile);
    }

    // ───────────────────────── LOGIN ─────────────────────────
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new CustomException("We couldn't find an account with this email address.", HttpStatus.NOT_FOUND));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash());

        if (!passwordMatches) {

            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction("LOGIN_FAILED");
            log.setEntity("AUTH");
            log.setEntityDetail("INCORRECT_PASSWORD");
            log.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(log);

            throw new CustomException("The password you entered is incorrect. Please try again.", HttpStatus.UNAUTHORIZED);
        }

        // Block login for specific statuses
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new CustomException("Your account registration was not approved. Please contact support.", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new CustomException("Your account has been suspended.", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == UserStatus.PENDING) {
            if (user.getRole() == UserRole.STAFF) {
                throw new CustomException("Your account is still pending approval from the property owner.", HttpStatus.FORBIDDEN);
            } else {
                throw new CustomException("Please verify your email address before logging in. Check your inbox for the OTP code we sent you.", HttpStatus.FORBIDDEN);
            }
        }

        AuditLog log = new AuditLog();
        log.setUser(user);
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
                user.getNationalIdUrl(),
                user.getStaffRole());

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
                .orElseThrow(() -> new CustomException("We couldn't find an account with this email address.", HttpStatus.NOT_FOUND));

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
                        () -> new CustomException("This password reset link is invalid or has expired. Please request a new one.", HttpStatus.BAD_REQUEST));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new CustomException("This password reset link has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Clear the token after successful reset
        passwordResetTokenRepository.delete(resetToken);

        // Log the action
        AuditLog log = new AuditLog();
        log.setUser(user);
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
                .orElseThrow(() -> new CustomException("We couldn't find an account with this email address.", HttpStatus.NOT_FOUND));

        VerificationOTP otp = verificationOTPRepository.findByUser(user)
                .orElseThrow(
                        () -> new CustomException("We couldn't find a valid verification code. Please request a new one.", HttpStatus.BAD_REQUEST));

        if (otp.isExpired()) {
            verificationOTPRepository.delete(otp);
            throw new CustomException("This verification code has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        if (!otp.getOtp().equals(code)) {
            throw new CustomException("The verification code you entered is incorrect. Please try again.", HttpStatus.BAD_REQUEST);
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
        log.setUser(user);
        log.setAction("EMAIL_VERIFIED");
        log.setEntity("AUTH");
        log.setEntityDetail(user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    // ───────────────────────── ROOM LOGIN ─────────────────────────
    public AuthResponse roomLogin(com.b4code.backend.dto.RoomLoginRequest request) {
        Long roomId = Long.parseLong(request.getRoomNumber());
        com.b4code.backend.models.Booking booking = bookingRepository.findActiveBookingByRoom(request.getPropertyId(), roomId)
                .orElseThrow(() -> new CustomException("We couldn't find an active reservation for this room number. Please check the number and try again.", HttpStatus.NOT_FOUND));

        // Strict name verification removed to allow family members to order.
        // We log them in using the primary booking email so the charge correctly routes to the room.
        
        User user = userRepository.findByEmail(booking.getGuestEmail().toLowerCase())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(booking.getGuestEmail().toLowerCase());
                    newUser.setFirstName(booking.getGuestName());
                    newUser.setLastName("");
                    newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString())); // dummy password
                    newUser.setRole(UserRole.GUEST);
                    newUser.setStatus(UserStatus.ACTIVE);
                    return userRepository.save(newUser);
                });

        // Ensure user is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction("ROOM_LOGIN_SUCCESS");
        log.setEntity("AUTH");
        log.setEntityDetail("Room: " + request.getRoomNumber());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        UserProfileDto profile = new UserProfileDto(
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getNationalIdUrl(),
                user.getStaffRole());

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
}
