package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.entity.StaffProperty;
import com.b4code.backend.modules.staff.repository.StaffPropertyRepository;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.admin.dao.PropertyRepository;
import com.b4code.backend.modules.qr.service.QRCodeService;
import com.b4code.backend.modules.qr.dto.QRCodeResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController("staffModuleStaffController")
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffPropertyRepository staffPropertyRepository;
    private final PropertyRepository propertyRepository;
    private final QRCodeService qrCodeService;

    // Get properties assigned to a staff member
    @GetMapping("/properties/{staffId}")
    public ResponseEntity<List<Property>> getStaffProperties(@PathVariable Long staffId) {
        List<StaffProperty> mappings = staffPropertyRepository.findByStaffId(staffId);
        List<Long> propertyIds = mappings.stream()
                .map(StaffProperty::getPropertyId)
                .collect(Collectors.toList());
        List<Property> properties = propertyRepository.findAllById(propertyIds);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/status")
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