package com.b4code.backend.dto;

import com.b4code.backend.models.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserDto {
    private String email;
    private UserRole role;
}
