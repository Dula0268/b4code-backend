package com.b4code.backend.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role; // "GUEST", "OWNER", "STAFF"
}
