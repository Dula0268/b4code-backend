package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.StaffDto.*;
import com.b4code.backend.modules.owner.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("ownerStaffController")
@RequestMapping("/api/owner/staff")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Owner — Staff", description = "Staff management endpoints")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @Operation(summary = "List staff by property")
    public ResponseEntity<StaffListResponse> listStaff(@RequestParam Long propertyId, @RequestParam(required = false) String role) {
        return ResponseEntity.ok(staffService.getStaffByProperty(propertyId, role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff profile")
    public ResponseEntity<StaffResponse> getStaff(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    @PostMapping
    @Operation(summary = "Add staff member")
    public ResponseEntity<StaffResponse> createStaff(@RequestBody StaffRequest request) {
        return ResponseEntity.ok(staffService.createStaff(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update staff member")
    public ResponseEntity<StaffResponse> updateStaff(@PathVariable Long id, @RequestBody StaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaff(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove staff member")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id); return ResponseEntity.noContent().build();
    }
}
