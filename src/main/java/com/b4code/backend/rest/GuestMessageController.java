package com.b4code.backend.rest;

import com.b4code.backend.dto.BookingMessageDto;
import com.b4code.backend.dto.BookingMessageRequest;
import com.b4code.backend.service.BookingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/bookings/{bookingId}/messages")
@RequiredArgsConstructor
public class GuestMessageController {

    private final BookingMessageService bookingMessageService;

    @GetMapping
    public ResponseEntity<List<BookingMessageDto>> getMessages(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingMessageService.getMessagesForBooking(bookingId));
    }

    @PostMapping
    public ResponseEntity<BookingMessageDto> sendMessage(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingMessageRequest request) {
        
        BookingMessageDto message = bookingMessageService.sendMessage(
                bookingId,
                null, // senderEmail will be handled by service
                "GUEST",
                request.getContent()
        );
        return ResponseEntity.ok(message);
    }
}
