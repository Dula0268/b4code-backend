package com.b4code.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
}
