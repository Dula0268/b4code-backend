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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Handle fullName split if firstName is not provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName() != null ? request.getLastName() : "");
        } else if (request.getPhone() != null) {
            // fullName came as one string — split it
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

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new AuthResponse(token, refreshToken, user.getEmail(), user.getRole().name(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new AuthResponse(token, refreshToken, user.getEmail(), user.getRole().name(), user.getId());
    }
}