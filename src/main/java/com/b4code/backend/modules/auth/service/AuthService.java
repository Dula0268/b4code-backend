package com.b4code.backend.modules.auth.service;

import com.b4code.backend.common.security.JwtUtil;
import com.b4code.backend.modules.auth.dto.AuthResponse;
import com.b4code.backend.modules.auth.dto.LoginRequest;
import com.b4code.backend.modules.auth.dto.RegisterRequest;
import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.b4code.backend.modules.admin.dao.AuditLogRepository;
import com.b4code.backend.modules.admin.models.AuditLog;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogRepository auditLogRepository;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
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

        // Default to GUEST if no role specified
        User.Role role;
        try {
            role = request.getRole() != null
                    ? User.Role.valueOf(request.getRole().toUpperCase())
                    : User.Role.GUEST;
        } catch (IllegalArgumentException e) {
            role = User.Role.GUEST;
        }
        user.setRole(role);

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

        return new AuthResponse(token, refreshToken, user.getEmail(), user.getRole().name(), user.getId(),
                user.getStatus().name());

    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        boolean success = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!success) {
            // LOG FAILED LOGIN
            AuditLog log = new AuditLog();
            log.setUserName(request.getEmail());
            log.setUserRole("UNKNOWN");
            log.setAction("LOGIN_FAILED");
            log.setEntity("AUTH");
            log.setEntityDetail(request.getEmail());
            log.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(log);

            throw new RuntimeException("Invalid email or password");
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

        return new AuthResponse(token, refreshToken, user.getEmail(),
                user.getRole().name(), user.getId(), user.getStatus().name());
    }
}