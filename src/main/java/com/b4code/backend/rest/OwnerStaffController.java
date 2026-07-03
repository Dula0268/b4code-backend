package com.b4code.backend.rest;

import com.b4code.backend.dto.StaffPendingResponse;
import com.b4code.backend.service.OwnerStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/owner/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerStaffController {

    private final OwnerStaffService ownerStaffService;

    @GetMapping("/pending")
    public ResponseEntity<List<StaffPendingResponse>> getPendingStaff(Principal principal) {
        return ResponseEntity.ok(ownerStaffService.getPendingStaff(principal.getName()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<StaffPendingResponse>> getAllStaff(Principal principal) {
        return ResponseEntity.ok(ownerStaffService.getAllStaff(principal.getName()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approveStaff(Principal principal, @PathVariable Long id) {
        ownerStaffService.approveStaff(principal.getName(), id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectStaff(Principal principal, @PathVariable Long id) {
        ownerStaffService.rejectStaff(principal.getName(), id);
        return ResponseEntity.ok().build();
    }
}
