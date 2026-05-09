package com.b4code.backend.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Role is required")
    private String role;

    // Owner fields
    private String propertyName;
    private String propertyAddress;
    private String nationalId;

    // Staff fields
    private String staffRole;
    private String employeeId;
    private String assignedProperty;
    private Long propertyId;
}