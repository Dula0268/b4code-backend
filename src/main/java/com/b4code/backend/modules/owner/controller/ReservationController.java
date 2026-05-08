package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.ReservationDto.*;
import com.b4code.backend.modules.owner.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/reservations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Owner — Reservations", description = "Reservation management endpoints")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "List reservations with KPIs")
    public ResponseEntity<ReservationKpiResponse> listReservations(@RequestParam(defaultValue = "1") Long ownerId, @RequestParam(required = false) String status, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reservationService.getReservationOverview(ownerId, status, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation details")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @PostMapping
    @Operation(summary = "Create manual booking")
    public ResponseEntity<ReservationResponse> createManualBooking(@RequestBody ManualBookingRequest request) {
        return ResponseEntity.ok(reservationService.createManualBooking(request));
    }

    @PatchMapping("/{id}/check-in")
    @Operation(summary = "Check-in guest")
    public ResponseEntity<ReservationResponse> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.updateStatus(id, "CHECKED_IN"));
    }

    @PatchMapping("/{id}/check-out")
    @Operation(summary = "Check-out guest")
    public ResponseEntity<ReservationResponse> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.updateStatus(id, "CHECKED_OUT"));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel reservation")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.updateStatus(id, "CANCELLED"));
    }
}
