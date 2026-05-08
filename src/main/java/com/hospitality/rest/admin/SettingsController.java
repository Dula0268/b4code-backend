package com.hospitality.rest.admin;

import com.hospitality.dto.admin.RolePermissionsDto;
import com.hospitality.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings/permissions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin — Settings", description = "Role permissions and system settings")
public class SettingsController {

    private final SettingsService settingsService;

    // GET /api/admin/settings/permissions/{role}
    @GetMapping("/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all grouped permissions for a specific role")
    public ResponseEntity<RolePermissionsDto> getRolePermissions(@PathVariable String role) {
        return ResponseEntity.ok(settingsService.getRolePermissions(role));
    }

    // PUT /api/admin/settings/permissions/{role}
    @PutMapping("/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update permission toggles for a role")
    public ResponseEntity<RolePermissionsDto> updateRolePermissions(
            @PathVariable String role,
            @RequestBody Map<String, Boolean> updates) {
        return ResponseEntity.ok(settingsService.updateRolePermissions(role, updates));
    }
}
