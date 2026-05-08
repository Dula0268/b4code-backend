package com.hospitality.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {
    private String role; // "GUEST", "OWNER", "STAFF", "ADMIN"
}