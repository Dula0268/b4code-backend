package com.hospitality.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;

    // Owner fields
    private String propertyName;
    private String propertyAddress;
    private String nationalId;

    // Staff fields
    private String staffRole;
    private String employeeId;
    private String assignedProperty;
}