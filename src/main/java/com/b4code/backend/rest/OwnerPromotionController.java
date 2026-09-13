package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.CreatePromoRequest;
import com.b4code.backend.dto.owner.PromoResponseDto;
import com.b4code.backend.service.OwnerPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/owner/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Promotions & Promo Codes")
public class OwnerPromotionController {

    private final OwnerPromotionService ownerPromotionService;

    @GetMapping
    @Operation(summary = "List promo codes for the authenticated owner's properties")
    public ResponseEntity<List<PromoResponseDto>> getPromotions(
            Principal principal,
            @RequestParam(required = false) Long propertyId) {

        return ResponseEntity.ok(ownerPromotionService.getPromotions(principal.getName(), propertyId));
    }

    @PostMapping
    @Operation(summary = "Create a new promo code")
    public ResponseEntity<PromoResponseDto> createPromotion(
            Principal principal,
            @Valid @RequestBody CreatePromoRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerPromotionService.createPromotion(principal.getName(), request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle active status of a promo code")
    public ResponseEntity<PromoResponseDto> togglePromotion(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(ownerPromotionService.togglePromotion(principal.getName(), id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an unused promo code")
    public ResponseEntity<Void> deletePromotion(
            Principal principal,
            @PathVariable Long id) {

        ownerPromotionService.deletePromotion(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
