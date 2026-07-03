package com.b4code.backend.rest;

import com.b4code.backend.dto.UserDto;
import com.b4code.backend.dto.UserPageDto;
import com.b4code.backend.dto.UserStatusUpdateDto;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Admin — User Management", description = "Endpoints to manage platform users")
public class AdminUserController {

    private final AdminUserService userService;

    // ── GET ALL USERS ─────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get all users", description = "Paginated list with optional search, role, and status filters")
    public ResponseEntity<UserPageDto> getAllUsers(
            @Parameter(description = "Search by name or email") @RequestParam(required = false, defaultValue = "") String search,

            @Parameter(description = "Filter by role: OWNER, STAFF, ADMIN, GUEST") @RequestParam(required = false) UserRole role,

            @Parameter(description = "Filter by status: ACTIVE, SUSPENDED") @RequestParam(required = false) UserStatus status,

            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Records per page") @RequestParam(defaultValue = "6") int size) {
        log.info("GET /api/admin/users — search='{}', role={}, status={}, page={}, size={}", search, role, status, page,
                size);

        // Safety guard: Ensure page index is never negative
        if (page < 0)
            page = 0;

        UserPageDto result = userService.getAllUsers(search, role, status, page, size);
        return ResponseEntity.ok(result);
    }

    // ── GET SINGLE USER ───────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get user by ID", description = "Returns full detail for a single user")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User's database ID") @PathVariable Long id) {
        log.info("GET /api/admin/users/{}", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // ── CREATE USER ───────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Creates a new user account. Password is hashed before storage.")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        log.info("POST /api/admin/users — email='{}'", request.getEmail());
        UserDto userDto = UserDto.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();
        UserDto created = userService.createUser(userDto, request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── UPDATE USER ───────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Updates name, email, or role. Supports partial updates.")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto) {
        log.info("PUT /api/admin/users/{}", id);
        UserDto updated = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updated);
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────────────────

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Toggles user account between ACTIVE and SUSPENDED")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusUpdateDto statusUpdate) {
        log.info("PUT /api/admin/users/{}/status — new status={}", id, statusUpdate.getStatus());
        UserDto updated = userService.updateUserStatus(id, statusUpdate);
        return ResponseEntity.ok(updated);
    }

    // ── DELETE USER ───────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Soft-deletes a user (sets deleted=true, does not remove the DB row)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("DELETE /api/admin/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // 204 — standard for successful DELETE
    }

    // ── Inner record for create request ──────────────────────────────────────

    public record CreateUserRequest(
            String firstName,
            String lastName,
            String email,
            UserRole role,
            String password) {
        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public UserRole getRole() {
            return role;
        }

        public String getPassword() {
            return password;
        }
    }

    // ── SEND RESET PASSWORD LINK ───────────────────────────────────────────────

    @PostMapping("/{id}/send-reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send reset password link", description = "Sends a password reset email to the user")
    public ResponseEntity<Void> sendResetPasswordLink(@PathVariable Long id) {
        log.info("POST /api/admin/users/{}/send-reset-password", id);
        userService.sendResetPasswordLink(id);
        return ResponseEntity.ok().build();
    }
}
