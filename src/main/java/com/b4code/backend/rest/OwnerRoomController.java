package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.OwnerRoomDto;
import com.b4code.backend.dto.owner.OwnerRoomListDto;
import com.b4code.backend.dto.owner.OwnerRoomRequest;
import com.b4code.backend.service.OwnerRoomService;
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
@Tag(name = "Owner — Rooms", description = "Owner-scoped room management")
public class OwnerRoomController {

    private final OwnerRoomService ownerRoomService;

    @GetMapping
    @Operation(summary = "List all rooms for the authenticated owner")
    public ResponseEntity<OwnerRoomListDto> listRooms(
            Principal principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(
                ownerRoomService.listRooms(principal.getName(), status, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single room by ID")
    public ResponseEntity<OwnerRoomDto> getRoom(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerRoomService.getRoom(principal.getName(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new room in one of the owner's properties")
    public ResponseEntity<OwnerRoomDto> createRoom(
            Principal principal,
            @RequestBody OwnerRoomRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerRoomService.createRoom(principal.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing room")
    public ResponseEntity<OwnerRoomDto> updateRoom(
            Principal principal,
            @PathVariable Long id,
            @RequestBody OwnerRoomRequest request) {

        return ResponseEntity.ok(ownerRoomService.updateRoom(principal.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a room")
    public ResponseEntity<Void> deleteRoom(
            Principal principal,
            @PathVariable Long id) {

        ownerRoomService.deleteRoom(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Set room operational status (AVAILABLE / OCCUPIED / MAINTENANCE)")
    public ResponseEntity<OwnerRoomDto> updateStatus(
            Principal principal,
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(ownerRoomService.updateStatus(principal.getName(), id, status));
    }

    @PatchMapping("/{id}/toggle-availability")
    @Operation(summary = "Toggle room isAvailable flag")
    public ResponseEntity<OwnerRoomDto> toggleAvailability(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerRoomService.toggleAvailability(principal.getName(), id));
    }
}
