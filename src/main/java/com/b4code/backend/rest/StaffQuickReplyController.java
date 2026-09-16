package com.b4code.backend.rest;

import com.b4code.backend.dto.StaffQuickReplyDto;
import com.b4code.backend.dto.StaffQuickReplyRequest;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.messaging.StaffQuickReply;
import com.b4code.backend.repository.StaffQuickReplyRepository;
import com.b4code.backend.dao.PropertyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner/properties/{propertyId}/staff-quick-replies")
@RequiredArgsConstructor
public class StaffQuickReplyController {

    private final StaffQuickReplyRepository staffQuickReplyRepository;
    private final PropertyRepository propertyRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<StaffQuickReplyDto>> getQuickReplies(@PathVariable Long propertyId) {
        List<StaffQuickReplyDto> replies = staffQuickReplyRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(replies);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<StaffQuickReplyDto> createQuickReply(
            @PathVariable Long propertyId,
            @Valid @RequestBody StaffQuickReplyRequest request) {
        
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        StaffQuickReply reply = StaffQuickReply.builder()
                .property(property)
                .name(request.getName())
                .message(request.getMessage())
                .isActive(request.getIsActive())
                .build();

        reply = staffQuickReplyRepository.save(reply);
        return ResponseEntity.ok(mapToDto(reply));
    }

    @PutMapping("/{replyId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<StaffQuickReplyDto> updateQuickReply(
            @PathVariable Long propertyId,
            @PathVariable Long replyId,
            @Valid @RequestBody StaffQuickReplyRequest request) {
        
        StaffQuickReply reply = staffQuickReplyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Quick Reply not found"));
                
        if (!reply.getProperty().getId().equals(propertyId)) {
            throw new RuntimeException("Quick Reply does not belong to this property");
        }

        reply.setName(request.getName());
        reply.setMessage(request.getMessage());
        reply.setIsActive(request.getIsActive());

        reply = staffQuickReplyRepository.save(reply);
        return ResponseEntity.ok(mapToDto(reply));
    }
    
    @DeleteMapping("/{replyId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteQuickReply(@PathVariable Long propertyId, @PathVariable Long replyId) {
        StaffQuickReply reply = staffQuickReplyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Quick Reply not found"));
        
        if (!reply.getProperty().getId().equals(propertyId)) {
            throw new RuntimeException("Quick Reply does not belong to this property");
        }
        
        staffQuickReplyRepository.delete(reply);
        return ResponseEntity.ok().build();
    }

    private StaffQuickReplyDto mapToDto(StaffQuickReply reply) {
        return StaffQuickReplyDto.builder()
                .id(reply.getId())
                .propertyId(reply.getProperty().getId())
                .name(reply.getName())
                .message(reply.getMessage())
                .isActive(reply.getIsActive())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
