package com.b4code.backend.modules.owner.availability;

/**
 * Owner Availability Module
 * ─────────────────────────
 *
 * Frontend pages:
 *   - owner/availability/weeklyCalendar/page.tsx     — weekly calendar view
 *   - owner/availability/monthlyCalendar/page.tsx    — monthly calendar view
 *   - owner/availability/bookingDetails/page.tsx     — booking detail view
 *   - owner/(Property)/properties/Availability       — property-level availability
 *
 * API Endpoints (planned):
 *   GET    /api/owner/availability/weekly         — weekly availability grid
 *   GET    /api/owner/availability/monthly        — monthly availability grid
 *   PUT    /api/owner/availability/bulk-update    — bulk update room availability
 *   GET    /api/owner/availability/booking/{id}   — get booking details
 *   PATCH  /api/owner/availability/{roomId}/block — block dates for a room
 *   PATCH  /api/owner/availability/{roomId}/open  — open dates for a room
 *
 * Implementation layers:
 *   - Controller: com.b4code.backend.modules.owner.controller.AvailabilityController
 *   - Service:    com.b4code.backend.modules.owner.service.AvailabilityService
 *   - DTO:        com.b4code.backend.modules.owner.dto.AvailabilityResponse
 *   - Entity:     com.b4code.backend.modules.owner.entity (RoomAvailability)
 *   - Repository: com.b4code.backend.modules.owner.repository (RoomAvailabilityRepository)
 */
public class Availability {

}
