package com.b4code.backend.rest;

import com.b4code.backend.dto.ActiveConversationDto;
import com.b4code.backend.dto.BookingMessageDto;
import com.b4code.backend.dto.BookingMessageRequest;
import com.b4code.backend.service.BookingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/messages")
@RequiredArgsConstructor
public class OwnerMessageController {

    private final BookingMessageService bookingMessageService;

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/property/{propertyId}/conversations")
    public ResponseEntity<List<ActiveConversationDto>> getConversations(@PathVariable Long propertyId) {
        return ResponseEntity.ok(bookingMessageService.getConversationsForProperty(propertyId, "OWNER"));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/conversations")
    public ResponseEntity<List<ActiveConversationDto>> getAllConversations(java.security.Principal principal) {
        return ResponseEntity.ok(bookingMessageService.getConversationsForOwner(principal.getName(), "OWNER"));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingMessageDto>> getMessages(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingMessageService.getMessagesForBooking(bookingId, "OWNER"));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<BookingMessageDto> sendMessage(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingMessageRequest request,
            java.security.Principal principal) {

        BookingMessageDto message = bookingMessageService.sendMessage(
                bookingId,
                principal.getName(),
                "OWNER",
                request.getContent(),
                "GUEST"
        );
        return ResponseEntity.ok(message);
    }
}
