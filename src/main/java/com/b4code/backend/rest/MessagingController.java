package com.b4code.backend.rest;

import com.b4code.backend.dto.MessageDTO.*;
import com.b4code.backend.service.MessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest/messages")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;

    /**
     * POST /api/v1/messages
     * Send a message in a booking conversation.
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {

        MessageResponse response = messagingService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/messages/conversation/{bookingId}
     * Get entire conversation for a booking.
     */
    @GetMapping("/conversation/{bookingId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(messagingService.getConversation(bookingId));
    }

    /**
     * PATCH /api/v1/messages/conversation/{bookingId}/mark-read
     * Mark all messages in a conversation as read.
     */
    @PatchMapping("/conversation/{bookingId}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long bookingId) {

        messagingService.markAsRead(bookingId);
        return ResponseEntity.noContent().build();
    }
}
