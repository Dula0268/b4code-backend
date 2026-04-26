package com.b4code.backend.modules.admin.rest;

import com.b4code.backend.modules.admin.dto.PropertyDto;
import com.b4code.backend.modules.admin.dto.PropertyPageDto;
import com.b4code.backend.modules.admin.dto.PropertyRejectionDto;
import com.b4code.backend.modules.admin.enums.PropertyStatus;
import com.b4code.backend.modules.admin.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/properties")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Admin — Property Verification", description = "Manage property onboarding requests")
public class PropertyController {

    private final PropertyService propertyService;

    // ── GET all (paginated + filtered)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List properties with optional search and status filter")
    public ResponseEntity<PropertyPageDto> getAllProperties(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) PropertyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        return ResponseEntity.ok(propertyService.getAllProperties(search, status, page, size));
    }

    // ── GET single
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get property detail by ID")
    public ResponseEntity<PropertyDto> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    // ── CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submit a new property for verification")
    public ResponseEntity<PropertyDto> createProperty(@RequestBody PropertyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.createProperty(dto));
    }

    // ── APPROVE
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a property")
    public ResponseEntity<PropertyDto> approveProperty(@PathVariable Long id) {
        log.info("PUT /api/admin/properties/{}/approve", id);
        return ResponseEntity.ok(propertyService.approveProperty(id));
    }

    // ── REJECT
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a property with a reason")
    public ResponseEntity<PropertyDto> rejectProperty(
            @PathVariable Long id,
            @RequestBody PropertyRejectionDto rejection
    ) {
        log.info("PUT /api/admin/properties/{}/reject", id);
        return ResponseEntity.ok(propertyService.rejectProperty(id, rejection));
    }

    // ── MARK UNDER REVIEW
    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Move property to Under Review")
    public ResponseEntity<PropertyDto> markUnderReview(@PathVariable Long id) {
        log.info("PUT /api/admin/properties/{}/review", id);
        return ResponseEntity.ok(propertyService.markUnderReview(id));
    }
}
