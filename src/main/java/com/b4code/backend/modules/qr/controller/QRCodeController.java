package com.b4code.backend.modules.qr.controller;

import com.b4code.backend.modules.qr.dto.QRCodeResponse;
import com.b4code.backend.modules.qr.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QRCodeController {
    
    private final QRCodeService qrCodeService;
    
    @PostMapping("/generate")
    public ResponseEntity<QRCodeResponse> generateQRCode(
            @RequestParam Long orderId,
            @RequestParam Long propertyId,
            @RequestParam(required = false) String description) {
        
        QRCodeResponse qrCode = qrCodeService.generateQRCode(orderId, propertyId, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(qrCode);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<QRCodeResponse> getQRCode(@PathVariable Long id) {
        QRCodeResponse qrCode = qrCodeService.getQRCodeById(id);
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
