package com.b4code.backend.modules.owner.reservation;

/**
 * Owner Reservation Module
 * ────────────────────────
 *
 * Frontend pages:
 *   - owner/reservation/page.tsx                                  — reservation listing
 *   - owner/reservation/reservationDetails/page.tsx               — reservation detail view
 *   - owner/reservation/reservationDetails/manualBooking/page.tsx — manual booking form
 *   - owner/(Property)/properties/Reservation                    — property-level reservations
 *
 * API Endpoints (planned):
 *   GET    /api/owner/reservations                     — list reservations (paginated, filtered)
 *   GET    /api/owner/reservations/{id}                — get reservation details
 *   POST   /api/owner/reservations                     — create manual booking
 *   PUT    /api/owner/reservations/{id}                — update reservation
 *   PATCH  /api/owner/reservations/{id}/status         — update status (confirm/check-in/check-out/cancel)
 *   PATCH  /api/owner/reservations/{id}/check-in       — check-in guest
 *   PATCH  /api/owner/reservations/{id}/check-out      — check-out guest
 *
 * Implementation layers:
 *   - Controller: com.b4code.backend.modules.owner.controller.ReservationController
 *   - Service:    com.b4code.backend.modules.owner.service.ReservationService
 *   - DTO:        com.b4code.backend.modules.owner.dto.ReservationResponse
 *   - Entity:     com.b4code.backend.modules.owner.entity.Reservation
 *   - Repository: com.b4code.backend.modules.owner.repository.ReservationRepository
 */
public class Reservation {

}
