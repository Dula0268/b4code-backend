package com.b4code.backend.modules.staff.qr.controller;

import com.b4code.backend.modules.staff.qr.dto.CreateQRRequest;
import com.b4code.backend.modules.staff.qr.dto.QRResponse;
import com.b4code.backend.modules.staff.qr.dto.QRValidationResponse;
import com.b4code.backend.modules.staff.qr.dto.UpdateQRRequest;
import com.b4code.backend.modules.staff.qr.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Slf4j
public class QRCodeController {

    private final QRCodeService qrCodeService;

    /**
     * Create a single QR code
     */
    @PostMapping("/generate")
    public ResponseEntity<QRResponse> generateQRCode(@Valid @RequestBody CreateQRRequest request) {
        log.info("Creating QR code: {}", request.getName());
        QRResponse response = qrCodeService.generateQRCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Bulk generate QR codes
     */
    @PostMapping("/bulk-generate")
    public ResponseEntity<List<QRResponse>> bulkGenerateQRCodes(
            @RequestParam Long propertyId,
            @Valid @RequestBody List<CreateQRRequest> requests) {
        log.info("Bulk generating {} QR codes for property: {}", requests.size(), propertyId);
        List<QRResponse> responses = qrCodeService.bulkGenerateQRCodes(propertyId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Get QR code by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<QRResponse> getQRCodeById(@PathVariable Long id) {
        log.info("Fetching QR code: {}", id);
        QRResponse response = qrCodeService.getQRCodeById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List QR codes for a property with pagination
     */
    @GetMapping("/list")
    public ResponseEntity<Page<QRResponse>> listQRCodesByProperty(
            @RequestParam Long propertyId,
            Pageable pageable) {
        log.info("Listing QR codes for property: {}", propertyId);
        Page<QRResponse> response = qrCodeService.getQRCodesByProperty(propertyId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * List active QR codes for a property with pagination
     */
    @GetMapping("/list/active")
    public ResponseEntity<Page<QRResponse>> listActiveQRCodesByProperty(
            @RequestParam Long propertyId,
            Pageable pageable) {
        log.info("Listing active QR codes for property: {}", propertyId);
        Page<QRResponse> response = qrCodeService.getActiveQRCodesByProperty(propertyId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update QR code
     */
    @PutMapping("/{id}")
    public ResponseEntity<QRResponse> updateQRCode(
            @PathVariable Long id,
            @RequestBody UpdateQRRequest request) {
        log.info("Updating QR code: {}", id);
        QRResponse response = qrCodeService.updateQRCode(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete QR code (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQRCode(@PathVariable Long id) {
        log.info("Deleting QR code: {}", id);
        qrCodeService.deleteQRCode(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle QR code status (ACTIVE <-> INACTIVE)
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<QRResponse> toggleQRStatus(@PathVariable Long id) {
        log.info("Toggling QR code status: {}", id);
        QRResponse response = qrCodeService.toggleQRStatus(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate QR code (called when guest scans QR)
     */
    @PostMapping("/validate")
    public ResponseEntity<QRValidationResponse> validateQRCode(@RequestParam String qrId) {
        log.info("Validating QR code: {}", qrId);
        QRValidationResponse response = qrCodeService.validateQRCode(qrId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get QR code image as PNG
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getQRImage(@PathVariable Long id) {
        log.info("Fetching QR image: {}", id);
        byte[] imageData = qrCodeService.getQRImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageData);
    }
}
