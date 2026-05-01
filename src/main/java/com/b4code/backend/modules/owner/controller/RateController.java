package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.RateDto.*;
import com.b4code.backend.modules.owner.service.RateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/rates")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Owner — Rates", description = "Rate plans, discounts, seasonal pricing")
public class RateController {

    private final RateService rateService;

    @GetMapping
    @Operation(summary = "Get rate overview with KPIs")
    public ResponseEntity<RateOverviewResponse> getRateOverview(@RequestParam Long propertyId) {
        return ResponseEntity.ok(rateService.getRateOverview(propertyId));
    }

    @PostMapping
    @Operation(summary = "Create rate plan")
    public ResponseEntity<RatePlanResponse> createRatePlan(@RequestBody RatePlanRequest request) {
        return ResponseEntity.ok(rateService.createRatePlan(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update rate plan")
    public ResponseEntity<RatePlanResponse> updateRatePlan(@PathVariable Long id, @RequestBody RatePlanRequest request) {
        return ResponseEntity.ok(rateService.updateRatePlan(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete rate plan")
    public ResponseEntity<Void> deleteRatePlan(@PathVariable Long id) {
        rateService.deleteRatePlan(id); return ResponseEntity.noContent().build();
    }

    @PostMapping("/discounts")
    @Operation(summary = "Create discount")
    public ResponseEntity<DiscountResponse> createDiscount(@RequestBody DiscountRequest request) {
        return ResponseEntity.ok(rateService.createDiscount(request));
    }

    @PutMapping("/discounts/{id}")
    @Operation(summary = "Update discount")
    public ResponseEntity<DiscountResponse> updateDiscount(@PathVariable Long id, @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(rateService.updateDiscount(id, request));
    }

    @DeleteMapping("/discounts/{id}")
    @Operation(summary = "Delete discount")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        rateService.deleteDiscount(id); return ResponseEntity.noContent().build();
    }
}
