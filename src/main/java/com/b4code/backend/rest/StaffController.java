package com.b4code.backend.rest;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.service.QRCodeService;
import com.b4code.backend.dto.QRCodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff: QR Management", description = "Endpoints for staff QR code management")
public class StaffController {

    private final PropertyRepository propertyRepository;
    private final QRCodeService qrCodeService;

    // QR Management endpoints
    @GetMapping("/qr/property/{propertyId}")
    @Operation(summary = "Get QR codes for a property", description = "Returns paginated QR code locations (tables, rooms, etc.) for management")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByProperty(
            @PathVariable Long propertyId,
            @RequestParam(required = false, defaultValue = "0") int skip,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<QRCodeResponse> qrCodes = qrCodeService.getQRCodesByProperty(propertyId);

        // Apply pagination
        int start = skip;
        int end = Math.min(skip + limit, qrCodes.size());
        List<QRCodeResponse> paginated = qrCodes.subList(start, Math.max(start, end));

        return ResponseEntity.ok(paginated);
    }
}
