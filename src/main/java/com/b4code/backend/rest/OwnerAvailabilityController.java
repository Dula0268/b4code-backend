package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.AvailabilityBulkUpdateRequest;
import com.b4code.backend.dto.owner.AvailabilityDayDto;
import com.b4code.backend.service.OwnerAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/owner/availability")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Availability")
public class OwnerAvailabilityController {

    private final OwnerAvailabilityService ownerAvailabilityService;

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly availability calendar for a property")
    public ResponseEntity<List<AvailabilityDayDto>> getWeeklyCalendar(
            Principal principal,
            @RequestParam Long propertyId,
            @RequestParam(required = false) String baseDate) {

        return ResponseEntity.ok(
                ownerAvailabilityService.getWeeklyCalendar(principal.getName(), propertyId, baseDate));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly availability calendar for a property")
    public ResponseEntity<List<AvailabilityDayDto>> getMonthlyCalendar(
            Principal principal,
            @RequestParam Long propertyId,
            @RequestParam int year,
            @RequestParam int month) {

        return ResponseEntity.ok(
                ownerAvailabilityService.getMonthlyCalendar(principal.getName(), propertyId, year, month));
    }

    @PutMapping("/bulk-update")
    @Operation(summary = "Bulk update availability status and prices for selected dates")
    public ResponseEntity<Void> bulkUpdate(
            Principal principal,
            @RequestBody AvailabilityBulkUpdateRequest request) {

        ownerAvailabilityService.bulkUpdate(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
