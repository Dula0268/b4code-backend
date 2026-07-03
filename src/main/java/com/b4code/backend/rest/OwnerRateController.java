package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.DiscountDto;
import com.b4code.backend.dto.owner.DiscountRequest;
import com.b4code.backend.dto.owner.RateOverviewDto;
import com.b4code.backend.dto.owner.RatePlanDto;
import com.b4code.backend.dto.owner.RatePlanRequest;
import com.b4code.backend.dto.owner.SeasonalPricingDto;
import com.b4code.backend.dto.owner.SeasonalPricingRequest;
import com.b4code.backend.service.OwnerRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/owner/rates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Rates")
public class OwnerRateController {

    private final OwnerRateService ownerRateService;

    @GetMapping
    @Operation(summary = "Get rate plans and discounts overview for a property")
    public ResponseEntity<RateOverviewDto> getRateOverview(
            Principal principal,
            @RequestParam Long propertyId) {

        return ResponseEntity.ok(ownerRateService.getRateOverview(principal.getName(), propertyId));
    }

    @PostMapping
    @Operation(summary = "Create a new rate plan")
    public ResponseEntity<RatePlanDto> createRatePlan(
            Principal principal,
            @RequestBody RatePlanRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerRateService.createRatePlan(principal.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a rate plan")
    public ResponseEntity<RatePlanDto> updateRatePlan(
            Principal principal,
            @PathVariable Long id,
            @RequestBody RatePlanRequest request) {

        return ResponseEntity.ok(ownerRateService.updateRatePlan(principal.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rate plan")
    public ResponseEntity<Void> deleteRatePlan(
            Principal principal,
            @PathVariable Long id) {

        ownerRateService.deleteRatePlan(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/discounts")
    @Operation(summary = "Create a discount")
    public ResponseEntity<DiscountDto> createDiscount(
            Principal principal,
            @RequestBody DiscountRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerRateService.createDiscount(principal.getName(), request));
    }

    @PutMapping("/discounts/{id}")
    @Operation(summary = "Update a discount")
    public ResponseEntity<DiscountDto> updateDiscount(
            Principal principal,
            @PathVariable Long id,
            @RequestBody DiscountRequest request) {

        return ResponseEntity.ok(ownerRateService.updateDiscount(principal.getName(), id, request));
    }

    @DeleteMapping("/discounts/{id}")
    @Operation(summary = "Delete a discount")
    public ResponseEntity<Void> deleteDiscount(
            Principal principal,
            @PathVariable Long id) {

        ownerRateService.deleteDiscount(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seasonal")
    @Operation(summary = "List seasonal pricing ranges for a property")
    public ResponseEntity<java.util.List<SeasonalPricingDto>> getSeasonalPricing(
            Principal principal,
            @RequestParam Long propertyId) {

        return ResponseEntity.ok(ownerRateService.getSeasonalPricing(principal.getName(), propertyId));
    }

    @PostMapping("/seasonal")
    @Operation(summary = "Create a seasonal pricing range")
    public ResponseEntity<SeasonalPricingDto> createSeasonalPricing(
            Principal principal,
            @RequestBody SeasonalPricingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerRateService.createSeasonalPricing(principal.getName(), request));
    }

    @DeleteMapping("/seasonal/{id}")
    @Operation(summary = "Delete a seasonal pricing range")
    public ResponseEntity<Void> deleteSeasonalPricing(
            Principal principal,
            @PathVariable Long id) {

        ownerRateService.deleteSeasonalPricing(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
