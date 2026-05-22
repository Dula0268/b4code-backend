package com.b4code.backend.dto;

import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.models.AdminUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public static UserDto fromEntity(AdminUser user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @JsonIgnore
    public AdminUser toEntity() {
        AdminUser user = new AdminUser();
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setRole(this.role);
        user.setStatus(this.status != null ? this.status : UserStatus.ACTIVE);
        return user;
    }
}

