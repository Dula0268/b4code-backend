package com.b4code.backend.rest;

import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dto.owner.OwnerRoomDto;
import com.b4code.backend.dto.owner.OwnerRoomListDto;
import com.b4code.backend.dto.owner.OwnerRoomRequest;
import com.b4code.backend.dto.owner.PhysicalRoomDto;
import com.b4code.backend.service.OwnerRoomService;

import java.util.List;
import java.util.Map;
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
    private final RoomRepository roomRepository;

    @GetMapping
    @Operation(summary = "List rooms for the authenticated owner with search and pagination")
    public ResponseEntity<OwnerRoomListDto> listRooms(
            Principal principal,
            @RequestParam(required = false)          String status,
            @RequestParam(required = false)          String search,
            @RequestParam(defaultValue = "1")        int page,
            @RequestParam(defaultValue = "10")       int size) {

        return ResponseEntity.ok(
                ownerRoomService.listRooms(principal.getName(), status, search, page, size));
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

    // ── Physical room units (individual door-numbered units) ─────────────────

    @GetMapping("/{id}/units")
    @Operation(summary = "List all physical units (door numbers) for a room type")
    public ResponseEntity<List<PhysicalRoomDto>> listPhysicalRooms(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerRoomService.listPhysicalRooms(principal.getName(), id));
    }

    @GetMapping("/by-property")
    @Operation(summary = "List room types for a property (for dropdown selection)")
    public ResponseEntity<List<Map<String, Object>>> listRoomsByProperty(
            @RequestParam Long propertyId) {

        List<Map<String, Object>> rooms = roomRepository.findByPropertyId(propertyId).stream()
                .map(r -> Map.<String, Object>of("id", r.getId(), "name", r.getName()))
                .toList();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/units/by-property")
    @Operation(summary = "List all physical units for every room in a property")
    public ResponseEntity<List<PhysicalRoomDto>> listPhysicalRoomsByProperty(
            Principal principal,
            @RequestParam Long propertyId) {

        return ResponseEntity.ok(ownerRoomService.listPhysicalRoomsByProperty(principal.getName(), propertyId));
    }

    @PatchMapping("/{id}/units/{unitId}/status")
    @Operation(summary = "Update the status of an individual physical unit (CLEAN / DIRTY / OUT_OF_ORDER)")
    public ResponseEntity<PhysicalRoomDto> updatePhysicalRoomStatus(
            Principal principal,
            @PathVariable Long id,
            @PathVariable Long unitId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                ownerRoomService.updatePhysicalRoomStatus(principal.getName(), id, unitId, status));
    }
}
