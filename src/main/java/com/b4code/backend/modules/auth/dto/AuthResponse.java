package com.b4code.backend.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String role;
    private Long userId;
    private String status;
    private Long propertyId;
    private UserProfileDto profile;
}