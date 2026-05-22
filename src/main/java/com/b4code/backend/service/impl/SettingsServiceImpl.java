package com.b4code.backend.service.impl;

import com.b4code.backend.modules.admin.common.annotation.Auditable;
import com.b4code.backend.dao.RolePermissionRepository;
import com.b4code.backend.modules.admin.dto.PermissionDto;
import com.b4code.backend.modules.admin.dto.RolePermissionsDto;
import com.b4code.backend.models.RolePermission;
import com.b4code.backend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public RolePermissionsDto getRolePermissions(String roleName) {
        List<RolePermission> permissions = rolePermissionRepository.findByRoleName(roleName);
        
        Map<String, List<PermissionDto>> grouped = permissions.stream()
                .collect(Collectors.groupingBy(
                        RolePermission::getSection,
                        Collectors.mapping(PermissionDto::fromEntity, Collectors.toList())
                ));
                
        return RolePermissionsDto.builder()
                .permissions(grouped)
                .build();
    }

    @Override
    @Transactional
    @Auditable(action = "Config Change", entity = "Role Permissions Update")
    public RolePermissionsDto updateRolePermissions(String roleName, Map<String, Boolean> updates) {
        List<RolePermission> permissions = rolePermissionRepository.findByRoleName(roleName);
        
        boolean changed = false;
        for (RolePermission p : permissions) {
            Boolean newValue = updates.get(p.getPermissionKey());
            if (newValue != null && p.isEnabled() != newValue) {
                p.setEnabled(newValue);
                changed = true;
            }
        }
        
        if (changed) {
            rolePermissionRepository.saveAll(permissions);
            log.info("Updated permissions for role: {}", roleName);
        }
        
        return getRolePermissions(roleName);
    }
}
