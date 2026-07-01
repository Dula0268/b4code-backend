package com.b4code.backend.rest;

import com.b4code.backend.dto.RolePermissionsDto;
import com.b4code.backend.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Role permissions and system settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final com.b4code.backend.dao.UserRepository userRepository;

    // ── Admin-only endpoints ─────────────────────────────────────────────────

    @GetMapping("/api/admin/settings/permissions/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all grouped permissions for a specific role (Admin only)")
    public ResponseEntity<RolePermissionsDto> getRolePermissions(@PathVariable String role) {
        return ResponseEntity.ok(settingsService.getRolePermissions(role));
    }

    @PutMapping("/api/admin/settings/permissions/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update permission toggles for a role (Admin only)")
    public ResponseEntity<RolePermissionsDto> updateRolePermissions(
            @PathVariable String role,
            @RequestBody Map<String, Boolean> updates) {
        return ResponseEntity.ok(settingsService.updateRolePermissions(role, updates));
    }

    // ── Self-service endpoint (Staff / Owner fetch their own permissions) ────

    @GetMapping("/api/settings/permissions/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get permissions for the currently authenticated user's role")
    public ResponseEntity<RolePermissionsDto> getMyPermissions(Authentication authentication) {
        // Derive role name from Spring Security authority (e.g. ROLE_STAFF → Staff)
        String rawRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_UNKNOWN")
                .replace("ROLE_", "");

        // Capitalise first letter only: STAFF → Staff, OWNER → Owner
        String roleName = rawRole.charAt(0) + rawRole.substring(1).toLowerCase();

        // If the role is Staff, they might have a specific sub-role (e.g., Kitchen Staff)
        if ("Staff".equals(roleName)) {
            com.b4code.backend.models.User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null && user.getStaffRole() != null && !user.getStaffRole().isBlank()) {
                roleName = user.getStaffRole();
            }
        }

        return ResponseEntity.ok(settingsService.getRolePermissions(roleName));
    }
}





