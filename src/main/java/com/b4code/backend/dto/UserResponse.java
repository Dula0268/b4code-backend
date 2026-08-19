package com.b4code.backend.dto;

import com.b4code.backend.models.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String phone;
    private Long propertyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String avatarUrl;
    private String nationalIdUrl;
    private String staffRole;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole().name());
        response.setPhone(user.getPhone());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setNationalIdUrl(user.getNationalIdUrl());
        response.setPropertyId(user.getPropertyId());
        response.setStaffRole(user.getStaffRole());
        return response;
    }
}


