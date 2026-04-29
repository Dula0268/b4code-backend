package com.b4code.backend.modules.owner.controller;

import com.b4code.backend.modules.owner.dto.MessageDto.*;
import com.b4code.backend.modules.owner.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/messages")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002", "http://localhost:3003", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Owner — Messages", description = "Messaging endpoints")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "List conversations")
    public ResponseEntity<MessageOverviewResponse> getConversations(@RequestParam(defaultValue = "1") Long ownerId) {
        return ResponseEntity.ok(messageService.getConversations(ownerId));
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "Get messages in a conversation")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getMessages(conversationId));
    }

    @PostMapping
    @Operation(summary = "Send a message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestParam(defaultValue = "1") Long ownerId, @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(ownerId, request));
    }

    @PutMapping("/{conversationId}/read")
    @Operation(summary = "Mark conversation as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId) {
        messageService.markAsRead(conversationId);
        return ResponseEntity.ok().build();
    }
}
