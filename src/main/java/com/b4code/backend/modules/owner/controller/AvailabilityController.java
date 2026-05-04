package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.AvailabilityDto.*;
import com.b4code.backend.modules.owner.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/owner/availability")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Owner — Availability", description = "Calendar availability endpoints")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly calendar")
    public ResponseEntity<WeeklyCalendarResponse> getWeeklyCalendar(@RequestParam Long propertyId, @RequestParam(required = false) String baseDate) {
        LocalDate date = (baseDate != null && !baseDate.trim().isEmpty()) ? LocalDate.parse(baseDate) : LocalDate.now();
        return ResponseEntity.ok(availabilityService.getWeeklyCalendar(propertyId, date));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly calendar")
    public ResponseEntity<MonthlyCalendarResponse> getMonthlyCalendar(@RequestParam Long propertyId, @RequestParam(defaultValue = "2024") int year, @RequestParam(defaultValue = "11") int month) {
        return ResponseEntity.ok(availabilityService.getMonthlyCalendar(propertyId, year, month));
    }

    @PutMapping("/bulk-update")
    @Operation(summary = "Bulk update availability status")
    public ResponseEntity<Void> bulkUpdate(@RequestBody AvailabilityUpdateRequest request) {
        availabilityService.updateAvailability(request);
        return ResponseEntity.ok().build();
    }
}
