package com.b4code.backend.rest;

import com.b4code.backend.dto.ChangePasswordRequest;
import com.b4code.backend.dto.UpdateProfileRequest;
import com.b4code.backend.dto.UpdateRoleRequest;
import com.b4code.backend.dto.UserResponse;
import com.b4code.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET all users - Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET user by ID - Admin only
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // UPDATE user role - Admin only
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    // DELETE user - Admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // GET current user profile
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userService.getUserByEmail(principal.getName()));
    }

    // UPDATE own profile
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            Principal principal,
            @RequestBody UpdateProfileRequest request) {
        UserResponse currentUser = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(userService.updateUserProfile(currentUser.getId(), request));
    }

    // CHANGE password
    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            Principal principal,
            @RequestBody ChangePasswordRequest request) {
        UserResponse currentUser = userService.getUserByEmail(principal.getName());
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }
}




