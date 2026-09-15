package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.ManualBookingRequest;
import com.b4code.backend.dto.owner.OwnerReservationDto;
import com.b4code.backend.service.OwnerExportService;
import com.b4code.backend.service.OwnerReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/owner/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Reservations")
public class OwnerReservationController {

    private final OwnerReservationService ownerReservationService;
    private final OwnerExportService ownerExportService;

    @GetMapping(value = "/export/pdf", produces = "application/pdf")
    @Operation(summary = "Export reservations to PDF")
    public ResponseEntity<byte[]> exportReservationsPdf(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        byte[] data = ownerExportService.exportReservationsToPdf(principal.getName(), search, status);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"reservations.pdf\"")
                .body(data);
    }

    @GetMapping
    @Operation(summary = "List all reservations for the authenticated owner's properties")
    public ResponseEntity<List<OwnerReservationDto>> listReservations(
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

    @PatchMapping("/{id}/late-arrival")
    @Operation(summary = "Toggle late arrival allowance for a reservation")
    public ResponseEntity<OwnerReservationDto> toggleLateArrival(
            Principal principal,
            @PathVariable Long id,
            @RequestParam boolean allowed) {

        return ResponseEntity.ok(ownerReservationService.toggleLateArrival(principal.getName(), id, allowed));
    }
}
