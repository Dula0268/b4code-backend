package com.b4code.backend.rest;

import com.b4code.backend.dto.InternalMessageDto;
import com.b4code.backend.dto.InternalMessageRequest;
import com.b4code.backend.dto.StaffQuickReplyDto;
import com.b4code.backend.service.InternalMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/staff/internal-messages/owner")
@RequiredArgsConstructor
public class InternalMessageController {

    private final InternalMessageService internalMessageService;

    @PreAuthorize("hasAnyRole('STAFF')")
    @GetMapping
    public ResponseEntity<List<InternalMessageDto>> getMessages(Principal principal) {
        return ResponseEntity.ok(internalMessageService.getStaffOwnerMessages(principal.getName()));
    }

    @PreAuthorize("hasAnyRole('STAFF')")
    @GetMapping("/quick-replies")
    public ResponseEntity<List<StaffQuickReplyDto>> getQuickReplies(Principal principal) {
        return ResponseEntity.ok(internalMessageService.getStaffQuickReplies(principal.getName()));
    }

    @PreAuthorize("hasAnyRole('STAFF')")
    @PostMapping
    public ResponseEntity<InternalMessageDto> sendMessage(
            @Valid @RequestBody InternalMessageRequest request,
            Principal principal) {
        return ResponseEntity.ok(internalMessageService.sendStaffMessageToOwner(principal.getName(), request.getContent()));
    }
}
