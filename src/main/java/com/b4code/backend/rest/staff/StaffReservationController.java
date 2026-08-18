package com.b4code.backend.rest.staff;

import com.b4code.backend.dto.owner.OwnerReservationDto;
import com.b4code.backend.service.BookingSseService;
import com.b4code.backend.service.StaffReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/properties/{propertyId}/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
@Tag(name = "Staff — Reservations")
public class StaffReservationController {

    private final StaffReservationService staffReservationService;
    private final BookingSseService bookingSseService;

    @GetMapping
    @Operation(summary = "List all reservations for the property")
    public ResponseEntity<List<OwnerReservationDto>> listReservations(
            Principal principal,
            @PathVariable Long propertyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                staffReservationService.listReservations(principal.getName(), propertyId, search, status));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time booking updates for the property via SSE")
    public SseEmitter streamReservations(@PathVariable Long propertyId) {
        return bookingSseService.addPropertyEmitter(propertyId);
    }

    @PatchMapping("/{id}/check-in")
    @Operation(summary = "Mark reservation as checked in")
    public ResponseEntity<OwnerReservationDto> checkIn(
            Principal principal,
            @PathVariable Long propertyId,
            @PathVariable Long id) {

        return ResponseEntity.ok(staffReservationService.checkIn(principal.getName(), propertyId, id));
    }

    @PatchMapping("/{id}/check-out")
    @Operation(summary = "Mark reservation as checked out (completed)")
    public ResponseEntity<OwnerReservationDto> checkOut(
            Principal principal,
            @PathVariable Long propertyId,
            @PathVariable Long id) {

        return ResponseEntity.ok(staffReservationService.checkOut(principal.getName(), propertyId, id));
    }

    @PostMapping("/{bookingId}/pay")
    @Operation(summary = "Take payment for a reservation")
    public ResponseEntity<OwnerReservationDto> takePayment(
            Principal principal,
            @PathVariable Long propertyId,
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> payload) {
        String nicNumber = payload.get("nicNumber");
        return ResponseEntity.ok(staffReservationService.takePayment(principal.getName(), propertyId, bookingId, nicNumber));
    }
}
