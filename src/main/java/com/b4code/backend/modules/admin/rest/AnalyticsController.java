package com.b4code.backend.modules.admin.rest;

import com.b4code.backend.modules.admin.dto.*;
import com.b4code.backend.modules.admin.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin — Analytics", description = "Platform-wide analytics data")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ── GET platform overview 
    @GetMapping("/platform")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Platform analytics — gross booking value, net revenue, occupancy, ADR, RevPAR")
    public ResponseEntity<PlatformAnalyticsDto> getPlatformAnalytics() {
        return ResponseEntity.ok(analyticsService.getPlatformAnalytics());
    }

    // ── GET stat cards 
    @GetMapping("/platform-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Platform summary — lead time, cancellations, bookings, users, listings, commission")
    public ResponseEntity<PlatformSummaryDto> getPlatformSummary() {
        return ResponseEntity.ok(analyticsService.getPlatformSummary());
    }

    // ── GET RevPAR breakdown
    @GetMapping("/revpar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "RevPAR breakdown per property")
    public ResponseEntity<List<RevParDto>> getRevPar() {
        return ResponseEntity.ok(analyticsService.getRevParBreakdown());
    }

    // ── GET bookings chart
    @GetMapping("/bookings-chart")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Monthly gross booking value array for the area chart")
    public ResponseEntity<List<BookingChartPointDto>> getBookingsChart() {
        return ResponseEntity.ok(analyticsService.getBookingsChart());
    }
}
