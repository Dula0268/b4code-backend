package com.b4code.backend.rest;

import com.b4code.backend.dto.InternalMessageDto;
import com.b4code.backend.dto.InternalMessageRequest;
import com.b4code.backend.dto.StaffConversationDto;
import com.b4code.backend.service.InternalMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/owner/internal-messages")
@RequiredArgsConstructor
public class OwnerInternalMessageController {

    private final InternalMessageService internalMessageService;

    @PreAuthorize("hasAnyRole('OWNER')")
    @GetMapping("/conversations")
    public ResponseEntity<List<StaffConversationDto>> getConversations(
            @RequestParam(required = false) Long propertyId,
            Principal principal) {
        return ResponseEntity.ok(internalMessageService.getOwnerStaffConversations(principal.getName(), propertyId));
    }

    @PreAuthorize("hasAnyRole('OWNER')")
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<InternalMessageDto>> getMessages(
            @PathVariable Long staffId,
            @RequestParam Long propertyId,
            Principal principal) {
        return ResponseEntity.ok(internalMessageService.getOwnerStaffMessages(principal.getName(), propertyId, staffId));
    }

    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping("/staff/{staffId}")
    public ResponseEntity<InternalMessageDto> sendMessage(
            @PathVariable Long staffId,
            @RequestParam Long propertyId,
            @Valid @RequestBody InternalMessageRequest request,
            Principal principal) {
        return ResponseEntity.ok(internalMessageService.sendOwnerMessageToStaff(principal.getName(), propertyId, staffId, request.getContent()));
    }
}
