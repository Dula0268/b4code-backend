package com.b4code.backend.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {
    private String role; // "GUEST", "OWNER", "STAFF", "ADMIN"
}