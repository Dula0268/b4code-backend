package com.b4code.backend.rest.staff;

import com.b4code.backend.dto.staff.analytics.OrderSummaryDto;
import com.b4code.backend.dto.staff.analytics.OrderTrendDto;
import com.b4code.backend.dto.staff.analytics.TopMenuItemDto;
import com.b4code.backend.service.staff.StaffAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/staff/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Staff Analytics", description = "Endpoints for staff to view analytics and performance")
public class StaffAnalyticsController {

    private final StaffAnalyticsService staffAnalyticsService;

    @Operation(summary = "Get order summary", description = "Fetch aggregated stats like total revenue, completion rate, AOV")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/orders/summary")
    public ResponseEntity<OrderSummaryDto> getOrderSummary(
            @RequestParam Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        
        return ResponseEntity.ok(staffAnalyticsService.getOrderSummary(propertyId, start, end));
    }

    @Operation(summary = "Get order trends", description = "Fetch time-series data for order volume and revenue")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/orders/trends")
    public ResponseEntity<List<OrderTrendDto>> getOrderTrends(
            @RequestParam Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String interval) {
        
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        
        return ResponseEntity.ok(staffAnalyticsService.getOrderTrends(propertyId, start, end, interval));
    }

    @Operation(summary = "Get top menu items", description = "Fetch best selling items by volume and revenue")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/menu/top-items")
    public ResponseEntity<List<TopMenuItemDto>> getTopMenuItems(
            @RequestParam Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit) {
        
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        
        return ResponseEntity.ok(staffAnalyticsService.getTopMenuItems(propertyId, start, end, limit));
    }
}
