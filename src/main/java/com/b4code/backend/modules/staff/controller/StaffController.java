package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.entity.StaffProperty;
import com.b4code.backend.modules.staff.repository.StaffPropertyRepository;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.admin.dao.PropertyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffPropertyRepository staffPropertyRepository;
    private final PropertyRepository propertyRepository;

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
}