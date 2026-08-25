package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.OwnerDashboardDto;
import com.b4code.backend.service.OwnerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/owner/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Dashboard")
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;

    @GetMapping
    @Operation(summary = "Get owner dashboard KPIs and recent bookings")
    public ResponseEntity<OwnerDashboardDto> getDashboard(
            Principal principal,
            @RequestParam(required = false) Integer calYear,
            @RequestParam(required = false) Integer calMonth) {
        return ResponseEntity.ok(ownerDashboardService.getDashboard(principal.getName(), calYear, calMonth));
    }
}
