package com.b4code.backend.modules.guest.rest;

import com.b4code.backend.modules.guest.dto.MessageDTO.*;
import com.b4code.backend.modules.guest.service.MessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/guest/messages")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Guest: Messaging", description = "Endpoints for guest-staff communication within a booking context")
public class MessagingController {

    private final MessagingService messagingService;

    /**
     * POST /api/guest/messages
     * Send a message in a booking conversation.
     */
    @PostMapping
    @Operation(summary = "Send a message", description = "Sends a new message to the staff regarding a specific booking")
    @ApiResponse(responseCode = "201", description = "Message sent successfully")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {

        MessageResponse response = messagingService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/guest/messages/conversation/{bookingId}
     * Get entire conversation for a booking.
     */
    @GetMapping("/conversation/{bookingId}")
    @Operation(summary = "Get conversation history", description = "Returns all messages exchanged for a specific booking")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(messagingService.getConversation(bookingId));
    }

    /**
     * PATCH /api/guest/messages/conversation/{bookingId}/mark-read
     * Mark all messages in a conversation as read.
     */
    @PatchMapping("/conversation/{bookingId}/mark-read")
    @Operation(summary = "Mark messages as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long bookingId) {

        messagingService.markAsRead(bookingId);
        return ResponseEntity.noContent().build();
    }
}
