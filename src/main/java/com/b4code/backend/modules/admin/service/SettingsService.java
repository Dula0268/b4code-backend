package com.b4code.backend.modules.admin.service;

import com.b4code.backend.modules.admin.dto.RolePermissionsDto;

import java.util.Map;

public interface SettingsService {
    
    RolePermissionsDto getRolePermissions(String roleName);
    
    RolePermissionsDto updateRolePermissions(String roleName, Map<String, Boolean> updates);
}
