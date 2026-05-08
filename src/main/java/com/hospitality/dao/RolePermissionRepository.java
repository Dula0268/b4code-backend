package com.hospitality.dao;

import com.hospitality.models.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    List<RolePermission> findByRoleName(String roleName);
    
    Optional<RolePermission> findByRoleNameAndPermissionKey(String roleName, String permissionKey);
}
