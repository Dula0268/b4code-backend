package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.SettingDto.*;
import com.b4code.backend.modules.owner.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/settings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Owner — Settings", description = "Settings management endpoints")
public class SettingController {

    private final SettingService settingService;

    // ── Property Settings ──
    @GetMapping("/property")
    @Operation(summary = "Get property settings")
    public ResponseEntity<PropertySettingResponse> getPropertySettings(@RequestParam(defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(settingService.getPropertySettings(ownerId));
    }

    @PutMapping("/property")
    @Operation(summary = "Update property settings")
    public ResponseEntity<PropertySettingResponse> updatePropertySettings(@RequestParam(defaultValue = "1") Long ownerId, @RequestBody PropertySettingUpdateRequest request) {
        return ResponseEntity.ok(settingService.updatePropertySettings(ownerId, request));
    }

    // ── Notifications ──
    @GetMapping("/notifications")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<NotificationPreferenceResponse> getNotifications(@RequestParam(defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(settingService.getNotificationPreferences(ownerId));
    }

    @PutMapping("/notifications")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreferenceResponse> updateNotifications(@RequestParam(defaultValue = "1") Long ownerId, @RequestBody NotificationPreferenceUpdateRequest request) {
        return ResponseEntity.ok(settingService.updateNotificationPreferences(ownerId, request));
    }

    // ── Billing / Bank Accounts ──
    @GetMapping("/billing")
    @Operation(summary = "List bank accounts")
    public ResponseEntity<List<BankAccountResponse>> getBankAccounts(@RequestParam(defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(settingService.getBankAccounts(ownerId));
    }

    @PostMapping("/billing/bank-account")
    @Operation(summary = "Add bank account")
    public ResponseEntity<BankAccountResponse> addBankAccount(@RequestParam(defaultValue = "1") Long ownerId, @RequestBody BankAccountRequest request) {
        return ResponseEntity.ok(settingService.addBankAccount(ownerId, request));
    }

    @DeleteMapping("/billing/bank-account/{id}")
    @Operation(summary = "Remove bank account")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable Long id) {
        settingService.deleteBankAccount(id); return ResponseEntity.noContent().build();
    }

    // ── Integrations ──
    @GetMapping("/integrations")
    @Operation(summary = "Get integrations")
    public ResponseEntity<List<IntegrationResponse>> getIntegrations(@RequestParam(defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(settingService.getIntegrations(ownerId));
    }

    // ── Reservation Restrictions ──
    @GetMapping("/property/restrictions")
    @Operation(summary = "List reservation restrictions")
    public ResponseEntity<List<RestrictionResponse>> getRestrictions(@RequestParam Long propertyId) {
        return ResponseEntity.ok(settingService.getRestrictions(propertyId));
    }

    @PostMapping("/property/restrictions")
    @Operation(summary = "Create restriction")
    public ResponseEntity<RestrictionResponse> createRestriction(@RequestBody RestrictionRequest request) {
        return ResponseEntity.ok(settingService.createRestriction(request));
    }

    @DeleteMapping("/property/restrictions/{id}")
    @Operation(summary = "Delete restriction")
    public ResponseEntity<Void> deleteRestriction(@PathVariable Long id) {
        settingService.deleteRestriction(id); return ResponseEntity.noContent().build();
    }
}
