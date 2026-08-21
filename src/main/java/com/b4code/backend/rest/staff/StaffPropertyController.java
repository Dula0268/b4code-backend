package com.b4code.backend.rest.staff;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.models.Property;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/staff/properties")
@RequiredArgsConstructor
@Slf4j
public class StaffPropertyController {

    private final PropertyRepository propertyRepository;

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/{id}/service-charge")
    public ResponseEntity<Map<String, Double>> getServiceCharge(@PathVariable Long id) {
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
            
        Double rate = property.getServiceChargeRate() != null ? property.getServiceChargeRate() : 10.0;
        return ResponseEntity.ok(Map.of("serviceChargeRate", rate));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PutMapping("/{id}/service-charge")
    public ResponseEntity<Map<String, Double>> updateServiceCharge(
            @PathVariable Long id,
            @RequestBody Map<String, Double> payload) {
            
        Double rate = payload.get("serviceChargeRate");
        if (rate == null) {
            rate = 10.0;
        }
        
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
            
        property.setServiceChargeRate(rate);
        propertyRepository.save(property);
        
        return ResponseEntity.ok(Map.of("serviceChargeRate", rate));
    }
}
