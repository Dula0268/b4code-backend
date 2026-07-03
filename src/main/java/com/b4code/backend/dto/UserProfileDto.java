package com.b4code.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
    private String nationalIdUrl;
    private String staffRole;
}

