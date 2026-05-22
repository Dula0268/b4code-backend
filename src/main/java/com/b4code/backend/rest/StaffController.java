package com.b4code.backend.rest;

import com.b4code.backend.models.StaffProperty;
import com.b4code.backend.dao.StaffPropertyRepository;
import com.b4code.backend.models.Property;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.service.QRCodeService;
import com.b4code.backend.dto.QRCodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff: Property & Context", description = "Endpoints for staff property assignments and context selection")
public class StaffController {

    private final StaffPropertyRepository staffPropertyRepository;
    private final PropertyRepository propertyRepository;
    private final QRCodeService qrCodeService;

    // Get properties assigned to a staff member
    @GetMapping("/properties/{staffId}")
    @Operation(summary = "Get properties assigned to a staff member", description = "Returns a list of properties where this staff member is authorized to work")
    public ResponseEntity<List<Property>> getStaffProperties(@PathVariable Long staffId) {
        List<StaffProperty> mappings = staffPropertyRepository.findByStaffId(staffId);
        List<Long> propertyIds = mappings.stream()
                .map(StaffProperty::getPropertyId)
                .collect(Collectors.toList());
        List<Property> properties = propertyRepository.findAllById(propertyIds);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/status")
    @Operation(summary = "Check staff status at a property", description = "Checks if a staff member has an active/pending session at a property")
    public ResponseEntity<?> checkStatus(@RequestParam Long staffId,
            @RequestParam Long propertyId) {

        Optional<StaffProperty> sp = staffPropertyRepository.findByStaffIdAndPropertyId(staffId, propertyId);

        if (sp.isPresent()) {
            return ResponseEntity.ok(sp.get().getStatus());
        } else {
            return ResponseEntity.ok("NOT_SELECTED");
        }
    }

    // Assign a property to a staff member (Admin only)
    @PostMapping("/properties")
    public ResponseEntity<StaffProperty> assignProperty(@RequestBody StaffProperty staffProperty) {
        StaffProperty saved = staffPropertyRepository.save(staffProperty);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/select-property")
    @Operation(summary = "Select a property to work at", description = "Requests to start a session at a specific property. Sets status to PENDING.")
    public ResponseEntity<?> selectProperty(@RequestParam Long staffId,
            @RequestParam Long propertyId) {

        var existing = staffPropertyRepository
                .findByStaffIdAndPropertyId(staffId, propertyId);

        StaffProperty sp = existing.orElseGet(StaffProperty::new);

        sp.setStaffId(staffId);
        sp.setPropertyId(propertyId);
        sp.setStatus(StaffProperty.Status.PENDING);

        staffPropertyRepository.save(sp);

        return ResponseEntity.ok("Waiting for approval");
    }

    // Remove a property from a staff member (Admin only)
    @DeleteMapping("/properties/{staffId}/{propertyId}")
    public ResponseEntity<Void> removeProperty(
            @PathVariable Long staffId,
            @PathVariable Long propertyId) {
        staffPropertyRepository.deleteByStaffIdAndPropertyId(staffId, propertyId);
        return ResponseEntity.noContent().build();
    }

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




