package com.b4code.backend.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class AcceptInviteRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    // Owner fields
    private String propertyName;
    private String propertyAddress;
    private String nationalId;
}
