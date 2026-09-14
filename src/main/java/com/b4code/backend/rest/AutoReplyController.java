package com.b4code.backend.rest;

import com.b4code.backend.dto.AutoReplyRuleDto;
import com.b4code.backend.dto.AutoReplyRuleRequest;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.messaging.AutoReplyRule;
import com.b4code.backend.repository.AutoReplyRuleRepository;
import com.b4code.backend.dao.PropertyRepository; // need to check if it's dao or repository
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.models.Booking;
import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff/properties/{propertyId}/auto-reply-rules")
@RequiredArgsConstructor
public class AutoReplyController {

    private final AutoReplyRuleRepository autoReplyRuleRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private void broadcastConfigUpdate(Long propertyId) {
        List<Map<String, Object>> staffRules = autoReplyRuleRepository.findByPropertyId(propertyId).stream()
                .filter(AutoReplyRule::getIsActive)
                .filter(r -> "STAFF".equalsIgnoreCase(r.getTargetRole()) || r.getTargetRole() == null)
                .map(r -> Map.<String, Object>of("id", r.getId(), "keyword", r.getKeyword()))
                .collect(Collectors.toList());
                
        List<Map<String, Object>> ownerRules = autoReplyRuleRepository.findByPropertyId(propertyId).stream()
                .filter(AutoReplyRule::getIsActive)
                .filter(r -> "OWNER".equalsIgnoreCase(r.getTargetRole()))
                .map(r -> Map.<String, Object>of("id", r.getId(), "keyword", r.getKeyword()))
                .collect(Collectors.toList());

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", "CONFIG_UPDATE");
        payload.put("staffRules", staffRules);
        payload.put("ownerRules", ownerRules);

        List<Booking> bookings = bookingRepository.findByPropertyId(propertyId);
        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.CHECKED_IN) {
                messagingTemplate.convertAndSend("/topic/booking/" + booking.getId(), payload);
                if (booking.getConfirmationCode() != null) {
                    messagingTemplate.convertAndSend("/topic/booking/" + booking.getConfirmationCode(), payload);
                }
            }
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    public ResponseEntity<List<AutoReplyRuleDto>> getRules(
            @PathVariable Long propertyId,
            @RequestParam(required = false, defaultValue = "STAFF") String role) {
        List<AutoReplyRuleDto> rules = autoReplyRuleRepository.findByPropertyId(propertyId)
                .stream()
                .filter(r -> role.equalsIgnoreCase(r.getTargetRole()) || (r.getTargetRole() == null && "STAFF".equalsIgnoreCase(role)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    public ResponseEntity<AutoReplyRuleDto> createRule(
            @PathVariable Long propertyId,
            @Valid @RequestBody AutoReplyRuleRequest request) {
        
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        AutoReplyRule rule = AutoReplyRule.builder()
                .property(property)
                .keyword(request.getKeyword())
                .replyMessage(request.getReplyMessage())
                .isActive(request.getIsActive())
                .targetRole(request.getTargetRole() != null ? request.getTargetRole().toUpperCase() : "STAFF")
                .build();

        rule = autoReplyRuleRepository.save(rule);
        broadcastConfigUpdate(propertyId);
        return ResponseEntity.ok(mapToDto(rule));
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    public ResponseEntity<AutoReplyRuleDto> updateRule(
            @PathVariable Long propertyId,
            @PathVariable Long ruleId,
            @Valid @RequestBody AutoReplyRuleRequest request) {
        
        AutoReplyRule rule = autoReplyRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
                
        if (!rule.getProperty().getId().equals(propertyId)) {
            throw new RuntimeException("Rule does not belong to this property");
        }

        rule.setKeyword(request.getKeyword());
        rule.setReplyMessage(request.getReplyMessage());
        rule.setIsActive(request.getIsActive());

        rule = autoReplyRuleRepository.save(rule);
        broadcastConfigUpdate(propertyId);
        return ResponseEntity.ok(mapToDto(rule));
    }
    
    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteRule(@PathVariable Long propertyId, @PathVariable Long ruleId) {
        AutoReplyRule rule = autoReplyRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        
        if (!rule.getProperty().getId().equals(propertyId)) {
            throw new RuntimeException("Rule does not belong to this property");
        }
        
        autoReplyRuleRepository.delete(rule);
        broadcastConfigUpdate(propertyId);
        return ResponseEntity.ok().build();
    }

    private AutoReplyRuleDto mapToDto(AutoReplyRule rule) {
        return AutoReplyRuleDto.builder()
                .id(rule.getId())
                .propertyId(rule.getProperty().getId())
                .keyword(rule.getKeyword())
                .replyMessage(rule.getReplyMessage())
                .isActive(rule.getIsActive())
                .targetRole(rule.getTargetRole())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
