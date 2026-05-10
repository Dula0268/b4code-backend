package com.b4code.backend.modules.admin.rest;

import com.b4code.backend.modules.admin.dto.DashboardKpiDto;
import com.b4code.backend.modules.admin.dto.RecentVerificationDto;
import com.b4code.backend.modules.admin.dto.RevenueTrendPointDto;
import com.b4code.backend.modules.admin.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminDashboardController")
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin — Dashboard", description = "Data for the admin home page")
public class DashboardController {

    private final DashboardService dashboardService;

    // ── GET KPI cards
    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Dashboard KPI cards — revenue, occupancy, active bookings")
    public ResponseEntity<DashboardKpiDto> getKpis() {
        return ResponseEntity.ok(dashboardService.getDashboardKpis());
    }

    // ── GET revenue trend chart
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Monthly revenue trend for dashboard chart")
    public ResponseEntity<List<RevenueTrendPointDto>> getRevenueTrend() {
        return ResponseEntity.ok(dashboardService.getDashboardRevenueTrend());
    }

    // ── GET recent verifications table
    @GetMapping("/recent-verifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Latest 5 pending/under-review property verification requests")
    public ResponseEntity<List<RecentVerificationDto>> getRecentVerifications() {
        return ResponseEntity.ok(dashboardService.getRecentVerifications());
    }
}
