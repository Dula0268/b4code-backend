package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.DashboardResponse;
import com.b4code.backend.modules.owner.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Owner — Dashboard", description = "Owner dashboard data endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/owner/dashboard
     *
     * Returns all dashboard data for the authenticated owner:
     * metric cards, availability preview, recent reservations, and alert cards.
     *
     * @param calYear  optional calendar year for the availability preview (defaults to current year)
     * @param calMonth optional calendar month (0-indexed, 0=January) for the availability preview
     * @param ownerId  optional owner ID override (for testing); in production, extracted from JWT
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get owner dashboard data", description = "Returns metric cards, availability calendar, recent reservations, and alert cards for the authenticated owner")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) Integer calYear,
            @RequestParam(required = false) Integer calMonth,
            @RequestParam(required = false) Long ownerId,
            Authentication authentication
    ) {
        Long resolvedOwnerId = resolveOwnerId(ownerId, authentication);
        log.info("GET /api/owner/dashboard — ownerId={}, calYear={}, calMonth={}", resolvedOwnerId, calYear, calMonth);

        DashboardResponse response = dashboardService.getDashboard(resolvedOwnerId, calYear, calMonth);
        return ResponseEntity.ok(response);
    }

    /**
     * Resolve the owner ID from the request param or JWT claims.
     * During development, ownerId can be passed as a query param.
     */
    private Long resolveOwnerId(Long ownerIdParam, Authentication authentication) {
        if (ownerIdParam != null) {
            return ownerIdParam;
        }
        // In production, extract from JWT/Authentication principal
        if (authentication != null && authentication.getName() != null) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.warn("Could not parse ownerId from authentication name: {}", authentication.getName());
            }
        }
        throw new RuntimeException("Owner ID could not be resolved. Please provide ownerId parameter or authenticate.");
    }
}
