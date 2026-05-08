package com.hospitality.service;

import com.hospitality.dto.admin.RolePermissionsDto;

import java.util.Map;

public interface SettingsService {
    
    RolePermissionsDto getRolePermissions(String roleName);
    
    RolePermissionsDto updateRolePermissions(String roleName, Map<String, Boolean> updates);
}
