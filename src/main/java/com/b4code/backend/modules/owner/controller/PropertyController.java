package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.PropertyDto.*;
import com.b4code.backend.modules.owner.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("ownerPropertyController")
@RequestMapping("/api/owner/properties")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Owner — Properties", description = "Property CRUD endpoints")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    @Operation(summary = "List owner properties")
    public ResponseEntity<PropertyListResponse> listProperties(
            @RequestParam(defaultValue = "1") Long ownerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(propertyService.listProperties(ownerId, page, size, search, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property details")
    public ResponseEntity<PropertyResponse> getProperty(@PathVariable Long id, @RequestParam(defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(propertyService.getPropertyDetail(id, ownerId));
    }

    @PostMapping
    @Operation(summary = "Create a new property")
    public ResponseEntity<PropertyResponse> createProperty(@RequestParam(defaultValue = "1") Long ownerId, @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.createProperty(ownerId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update property")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable Long id, @RequestParam(defaultValue = "1") Long ownerId, @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, ownerId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete property")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id, @RequestParam(defaultValue = "1") Long ownerId) {
        propertyService.deleteProperty(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle property active/inactive")
    public ResponseEntity<PropertyResponse> toggleStatus(@PathVariable Long id, @RequestParam(defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(propertyService.togglePropertyStatus(id, ownerId));
    }

    @GetMapping("/{id}/media")
    @Operation(summary = "List property media")
    public ResponseEntity<List<MediaResponse>> listMedia(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.listMedia(id));
    }
}
