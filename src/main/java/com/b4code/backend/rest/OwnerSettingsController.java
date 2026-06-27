package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.BankAccountDto;
import com.b4code.backend.dto.owner.BankAccountRequest;
import com.b4code.backend.dto.owner.NotificationPrefDto;
import com.b4code.backend.dto.owner.PropertySettingDto;
import com.b4code.backend.dto.owner.ReservationRestrictionDto;
import com.b4code.backend.dto.owner.RestrictionRequest;
import com.b4code.backend.service.OwnerSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/owner/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Settings")
public class OwnerSettingsController {

    private final OwnerSettingsService ownerSettingsService;

    @GetMapping("/billing")
    @Operation(summary = "Get all bank accounts for the owner")
    public ResponseEntity<List<BankAccountDto>> getBankAccounts(Principal principal) {
        return ResponseEntity.ok(ownerSettingsService.getBankAccounts(principal.getName()));
    }

    @PostMapping("/billing/bank-account")
    @Operation(summary = "Add a new bank account")
    public ResponseEntity<BankAccountDto> addBankAccount(
            Principal principal,
            @RequestBody BankAccountRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerSettingsService.addBankAccount(principal.getName(), request));
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get notification preferences for the owner")
    public ResponseEntity<NotificationPrefDto> getNotificationPrefs(Principal principal) {
        return ResponseEntity.ok(ownerSettingsService.getNotificationPrefs(principal.getName()));
    }

    @PutMapping("/notifications")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPrefDto> updateNotificationPrefs(
            Principal principal,
            @RequestBody NotificationPrefDto dto) {

        return ResponseEntity.ok(ownerSettingsService.updateNotificationPrefs(principal.getName(), dto));
    }

    @GetMapping("/property")
    @Operation(summary = "Get property booking settings")
    public ResponseEntity<PropertySettingDto> getPropertySettings(
            Principal principal,
            @RequestParam Long propertyId) {

        return ResponseEntity.ok(ownerSettingsService.getPropertySettings(principal.getName(), propertyId));
    }

    @PutMapping("/property")
    @Operation(summary = "Update property booking settings")
    public ResponseEntity<PropertySettingDto> updatePropertySettings(
            Principal principal,
            @RequestParam Long propertyId,
            @RequestBody PropertySettingDto dto) {

        return ResponseEntity.ok(
                ownerSettingsService.updatePropertySettings(principal.getName(), propertyId, dto));
    }

    @GetMapping("/restrictions")
    @Operation(summary = "List reservation restrictions for a property")
    public ResponseEntity<List<ReservationRestrictionDto>> getRestrictions(
            Principal principal,
            @RequestParam Long propertyId) {

        return ResponseEntity.ok(ownerSettingsService.getRestrictions(principal.getName(), propertyId));
    }

    @PostMapping("/restrictions")
    @Operation(summary = "Create a reservation restriction")
    public ResponseEntity<ReservationRestrictionDto> createRestriction(
            Principal principal,
            @RequestBody RestrictionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerSettingsService.createRestriction(principal.getName(), request));
    }

    @PutMapping("/restrictions/{id}")
    @Operation(summary = "Update a reservation restriction")
    public ResponseEntity<ReservationRestrictionDto> updateRestriction(
            Principal principal,
            @PathVariable Long id,
            @RequestBody RestrictionRequest request) {

        return ResponseEntity.ok(ownerSettingsService.updateRestriction(principal.getName(), id, request));
    }

    @DeleteMapping("/restrictions/{id}")
    @Operation(summary = "Delete a reservation restriction")
    public ResponseEntity<Void> deleteRestriction(
            Principal principal,
            @PathVariable Long id) {

        ownerSettingsService.deleteRestriction(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
