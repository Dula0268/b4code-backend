package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.OwnerPropertyDto;
import com.b4code.backend.dto.owner.OwnerPropertyPageDto;
import com.b4code.backend.dto.owner.OwnerPropertyRequest;
import com.b4code.backend.service.OwnerPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/owner/properties")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Properties", description = "Owner-scoped property management")
public class OwnerPropertyController {

    private final OwnerPropertyService ownerPropertyService;

    @GetMapping
    @Operation(summary = "List all properties for the authenticated owner")
    public ResponseEntity<OwnerPropertyPageDto> listProperties(
            Principal principal,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    String search,
            @RequestParam(required = false)    String status) {

        return ResponseEntity.ok(
                ownerPropertyService.listProperties(principal.getName(), page, size, search, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single property by ID")
    public ResponseEntity<OwnerPropertyDto> getProperty(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerPropertyService.getProperty(principal.getName(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new property listing (submitted for admin approval)")
    public ResponseEntity<OwnerPropertyDto> createProperty(
            Principal principal,
            @RequestBody OwnerPropertyRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerPropertyService.createProperty(principal.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing property")
    public ResponseEntity<OwnerPropertyDto> updateProperty(
            Principal principal,
            @PathVariable Long id,
            @RequestBody OwnerPropertyRequest request) {

        return ResponseEntity.ok(ownerPropertyService.updateProperty(principal.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a property")
    public ResponseEntity<Void> deleteProperty(
            Principal principal,
            @PathVariable Long id) {

        ownerPropertyService.deleteProperty(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle property between ACTIVE and INACTIVE")
    public ResponseEntity<OwnerPropertyDto> toggleStatus(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerPropertyService.toggleStatus(principal.getName(), id));
    }
}
