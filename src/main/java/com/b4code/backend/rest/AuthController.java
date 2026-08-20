package com.b4code.backend.rest;

import com.b4code.backend.dto.*;
import com.b4code.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new ForgotPasswordResponse(
                "If that email is registered, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or expired reset link. Please request a new one.");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok("Email verified successfully.");
    }

    @PostMapping("/roomType-login")
    public ResponseEntity<AuthResponse> roomLogin(@RequestBody RoomLoginRequest request) {
        return ResponseEntity.ok(authService.roomLogin(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody java.util.Map<String, String> body) {
        authService.resendOtp(body.get("email"));
        return ResponseEntity.ok("A new verification code has been sent to your email.");
    }
}
