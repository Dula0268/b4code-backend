package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.OwnerRoomTypeDto;
import com.b4code.backend.dto.owner.OwnerRoomTypeListDto;
import com.b4code.backend.dto.owner.OwnerRoomTypeRequest;
import com.b4code.backend.service.OwnerRoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/owner/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — RoomTypes", description = "Owner-scoped roomType management")
public class OwnerRoomTypeController {

    private final OwnerRoomTypeService ownerRoomTypeService;

    @GetMapping
    @Operation(summary = "List all roomTypes for the authenticated owner")
    public ResponseEntity<OwnerRoomTypeListDto> listRoomTypes(
            Principal principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(
                ownerRoomTypeService.listRoomTypes(principal.getName(), status, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single roomType by ID")
    public ResponseEntity<OwnerRoomTypeDto> getRoom(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerRoomTypeService.getRoomType(principal.getName(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new roomType in one of the owner's properties")
    public ResponseEntity<OwnerRoomTypeDto> createRoom(
            Principal principal,
            @RequestBody OwnerRoomTypeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerRoomTypeService.createRoom(principal.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing roomType")
    public ResponseEntity<OwnerRoomTypeDto> updateRoom(
            Principal principal,
            @PathVariable Long id,
            @RequestBody OwnerRoomTypeRequest request) {

        return ResponseEntity.ok(ownerRoomTypeService.updateRoom(principal.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a roomType")
    public ResponseEntity<Void> deleteRoom(
            Principal principal,
            @PathVariable Long id) {

        ownerRoomTypeService.deleteRoom(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Set roomType operational status (AVAILABLE / OCCUPIED / MAINTENANCE)")
    public ResponseEntity<OwnerRoomTypeDto> updateStatus(
            Principal principal,
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(ownerRoomTypeService.updateStatus(principal.getName(), id, status));
    }

    @PatchMapping("/{id}/toggle-availability")
    @Operation(summary = "Toggle roomType isAvailable flag")
    public ResponseEntity<OwnerRoomTypeDto> toggleAvailability(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerRoomTypeService.toggleAvailability(principal.getName(), id));
    }
}
