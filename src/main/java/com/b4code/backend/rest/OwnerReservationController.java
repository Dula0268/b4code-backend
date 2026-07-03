package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.ManualBookingRequest;
import com.b4code.backend.dto.owner.OwnerReservationDto;
import com.b4code.backend.dto.owner.OwnerReservationPageDto;
import com.b4code.backend.service.OwnerReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/owner/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Reservations")
public class OwnerReservationController {

    private final OwnerReservationService ownerReservationService;

    @GetMapping
    @Operation(summary = "List all reservations with KPI summary")
    public ResponseEntity<OwnerReservationPageDto> listReservations(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                ownerReservationService.listReservations(principal.getName(), search, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single reservation by ID")
    public ResponseEntity<OwnerReservationDto> getReservation(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerReservationService.getReservation(principal.getName(), id));
    }

    @PostMapping
    @Operation(summary = "Create a manual booking on behalf of a guest")
    public ResponseEntity<OwnerReservationDto> createManualBooking(
            Principal principal,
            @RequestBody ManualBookingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerReservationService.createManualBooking(principal.getName(), request));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending reservation")
    public ResponseEntity<OwnerReservationDto> confirm(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerReservationService.confirm(principal.getName(), id));
    }

    @PatchMapping("/{id}/check-in")
    @Operation(summary = "Mark reservation as checked in")
    public ResponseEntity<OwnerReservationDto> checkIn(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerReservationService.checkIn(principal.getName(), id));
    }

    @PatchMapping("/{id}/check-out")
    @Operation(summary = "Mark reservation as checked out (completed)")
    public ResponseEntity<OwnerReservationDto> checkOut(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerReservationService.checkOut(principal.getName(), id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<OwnerReservationDto> cancel(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerReservationService.cancel(principal.getName(), id));
    }
}
