package com.b4code.backend.rest;

import com.b4code.backend.dto.ActiveConversationDto;
import com.b4code.backend.dto.BookingMessageDto;
import com.b4code.backend.dto.BookingMessageRequest;
import com.b4code.backend.service.BookingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/messages")
@RequiredArgsConstructor
public class StaffMessageController {

    private final BookingMessageService bookingMessageService;

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/property/{propertyId}/conversations")
    public ResponseEntity<List<ActiveConversationDto>> getConversations(@PathVariable Long propertyId) {
        return ResponseEntity.ok(bookingMessageService.getConversationsForProperty(propertyId));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingMessageDto>> getMessages(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingMessageService.getMessagesForBooking(bookingId));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<BookingMessageDto> sendMessage(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingMessageRequest request,
            java.security.Principal principal) {
        
        BookingMessageDto message = bookingMessageService.sendMessage(
                bookingId,
                principal.getName(),
                "STAFF",
                request.getContent()
        );
        return ResponseEntity.ok(message);
    }
}
