package com.b4code.backend.rest;

import com.b4code.backend.dto.QRCodeGenerateRequest;
import com.b4code.backend.dto.QRCodeResponse;
import com.b4code.backend.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Tag(name = "Staff: QR Management", description = "Endpoints for generating and managing property QR codes")
public class QRCodeController {
    
    private final QRCodeService qrCodeService;
    
    @GetMapping("/list")
    @Operation(summary = "List QR codes for a property (Paginated)", description = "Returns a paginated list of QR codes assigned to a specific property")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesList(
            @RequestParam Long propertyId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        List<QRCodeResponse> qrCodes = qrCodeService.getQRCodesByPropertyPaginated(propertyId, page, size);
        return ResponseEntity.ok(qrCodes);
    }
    
    @PostMapping("/generate")
    @Operation(summary = "Generate a new QR code", description = "Creates a new QR code location and generates the physical QR image")
    public ResponseEntity<QRCodeResponse> generateQRCode(@RequestBody QRCodeGenerateRequest request) {
        QRCodeResponse qrCode = qrCodeService.generateQRCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(qrCode);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<QRCodeResponse> getQRCode(@PathVariable Long id) {
        QRCodeResponse qrCode = qrCodeService.getQRCodeById(id);
        return ResponseEntity.ok(qrCode);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<QRCodeResponse> updateQRCode(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        QRCodeResponse qrCode = qrCodeService.updateQRCode(id, updates);
        return ResponseEntity.ok(qrCode);
    }
    
    @PostMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle QR status", description = "Enable or disable a QR code for guest scanning")
    public ResponseEntity<QRCodeResponse> toggleQRCodeStatus(@PathVariable Long id) {
        QRCodeResponse qrCode = qrCodeService.toggleQRCodeStatus(id);
        return ResponseEntity.ok(qrCode);
    }
    
    @GetMapping("/value/{qrCodeValue}")
    public ResponseEntity<?> getQRCodeByValue(@PathVariable String qrCodeValue) {
        return qrCodeService.getQRCodeByValue(qrCodeValue)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByProperty(@PathVariable Long propertyId) {
        List<QRCodeResponse> qrCodes = qrCodeService.getQRCodesByProperty(propertyId);
        return ResponseEntity.ok(qrCodes);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByOrder(@PathVariable Long orderId) {
        List<QRCodeResponse> qrCodes = qrCodeService.getQRCodesByOrder(orderId);
        return ResponseEntity.ok(qrCodes);
    }
    
    @GetMapping("/property/{propertyId}/status/{status}")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByPropertyAndStatus(
            @PathVariable Long propertyId,
            @PathVariable String status) {
        
        List<QRCodeResponse> qrCodes = qrCodeService.getQRCodesByPropertyAndStatus(propertyId, status);
        return ResponseEntity.ok(qrCodes);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<QRCodeResponse> updateQRCodeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        QRCodeResponse updated = qrCodeService.updateQRCodeStatus(id, status);
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}/scan")
    public ResponseEntity<QRCodeResponse> markQRCodeAsScanned(@PathVariable Long id) {
        QRCodeResponse updated = qrCodeService.markQRCodeAsScanned(id);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQRCode(@PathVariable Long id) {
        qrCodeService.deleteQRCode(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<Void> deleteQRCodesByOrder(@PathVariable Long orderId) {
        qrCodeService.deleteQRCodesByOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}




