package com.b4code.backend.modules.qr.controller;

import com.b4code.backend.modules.qr.dto.QRCodeGenerateRequest;
import com.b4code.backend.modules.qr.dto.QRCodeResponse;
import com.b4code.backend.modules.qr.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QRCodeController {
    
    private final QRCodeService qrCodeService;
    private final com.b4code.backend.modules.qr.repository.QRCodeRepository qrCodeRepository;
    private final com.b4code.backend.modules.admin.dao.PropertyRepository propertyRepository;
    
    /**
     * Validate a QR code scanned by a guest.
     * Returns the QR context (propertyId, propertyName, location, type) so
     * the guest frontend can fetch the correct menu.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateQRCode(@RequestParam String qrId) {
        var qrOpt = qrCodeRepository.findByUniqueQrId(qrId);
        if (qrOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "QR code not found. Please scan a valid code."));
        }
        
        var qr = qrOpt.get();
        if (!"ACTIVE".equalsIgnoreCase(qr.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "This QR code is no longer active."));
        }
        
        // Look up the property name
        String propertyName = "Property";
        if (qr.getPropertyId() != null) {
            var propOpt = propertyRepository.findById(qr.getPropertyId());
            if (propOpt.isPresent()) {
                propertyName = propOpt.get().getName();
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "qrId", qr.getUniqueQrId(),
                "propertyId", qr.getPropertyId() != null ? qr.getPropertyId() : 0,
                "propertyName", propertyName,
                "locationLabel", qr.getLocation() != null ? qr.getLocation() : "",
                "type", qr.getType() != null ? qr.getType() : "DINING_TABLE",
                "name", qr.getName() != null ? qr.getName() : "",
                "status", qr.getStatus()
        ));
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesList(
            @RequestParam Long propertyId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        List<QRCodeResponse> qrCodes = qrCodeService.getQRCodesByPropertyPaginated(propertyId, page, size);
        return ResponseEntity.ok(qrCodes);
    }
    
    @PostMapping("/generate")
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
