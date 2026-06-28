package com.b4code.backend.rest;

import com.b4code.backend.dto.owner.OwnerConversationDto;
import com.b4code.backend.dto.owner.OwnerMessageDto;
import com.b4code.backend.service.OwnerMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner — Messages")
public class OwnerMessageController {

    private final OwnerMessageService ownerMessageService;

    @GetMapping
    @Operation(summary = "List all guest conversations for the owner")
    public ResponseEntity<List<OwnerConversationDto>> getConversations(Principal principal) {
        return ResponseEntity.ok(ownerMessageService.getConversations(principal.getName()));
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "Get all messages in a conversation (booking thread)")
    public ResponseEntity<List<OwnerMessageDto>> getMessages(
            Principal principal,
            @PathVariable Long conversationId) {

        return ResponseEntity.ok(ownerMessageService.getMessages(principal.getName(), conversationId));
    }

    @PostMapping
    @Operation(summary = "Send a message to a guest")
    public ResponseEntity<OwnerMessageDto> sendMessage(
            Principal principal,
            @RequestBody Map<String, Object> body) {

        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        String content = body.get("content").toString();
        return ResponseEntity.ok(ownerMessageService.sendMessage(principal.getName(), conversationId, content));
    }

    @PutMapping("/{conversationId}/read")
    @Operation(summary = "Mark all guest messages in a conversation as read")
    public ResponseEntity<Void> markAsRead(
            Principal principal,
            @PathVariable Long conversationId) {

        ownerMessageService.markAsRead(principal.getName(), conversationId);
        return ResponseEntity.noContent().build();
    }
}
