package com.b4code.backend.modules.owner.room_management;

/**
 * Owner Room Management Module
 * ────────────────────────────
 *
 * Frontend pages:
 *   - owner/(room & inventry)/roomManagement/page.tsx      — room listing
 *   - owner/(room & inventry)/roomManagement/addRoom       — add new room
 *
 * API Endpoints (planned):
 *   GET    /api/owner/rooms                    — list all rooms across properties
 *   GET    /api/owner/rooms/{id}               — get room details
 *   POST   /api/owner/rooms                    — create a new room
 *   PUT    /api/owner/rooms/{id}               — update room details
 *   DELETE /api/owner/rooms/{id}               — delete a room
 *   PATCH  /api/owner/rooms/{id}/status        — update room status (available/maintenance/blocked)
 *
 * Implementation layers:
 *   - Controller: com.b4code.backend.modules.owner.controller.RoomController
 *   - Service:    com.b4code.backend.modules.owner.service.RoomService
 *   - DTO:        com.b4code.backend.modules.owner.dto.RoomResponse
 *   - Entity:     com.b4code.backend.modules.owner.entity.Room
 *   - Repository: com.b4code.backend.modules.owner.repository.RoomRepository
 */
public class RoomManagement {

}
