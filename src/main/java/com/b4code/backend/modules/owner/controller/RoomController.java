package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.RoomDto.*;
import com.b4code.backend.modules.owner.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/rooms")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Owner — Rooms", description = "Room management endpoints")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "List rooms with KPI stats")
    public ResponseEntity<RoomKpiResponse> listRooms(@RequestParam(defaultValue = "1") Long ownerId, @RequestParam(required = false) String status, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(roomService.getRoomOverview(ownerId, status, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room details")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new room")
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update room")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete room")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update room status")
    public ResponseEntity<RoomResponse> updateRoomStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(roomService.updateRoomStatus(id, status));
    }
}
