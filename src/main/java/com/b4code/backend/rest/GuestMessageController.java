package com.b4code.backend.rest;

import com.b4code.backend.dto.BookingMessageDto;
import com.b4code.backend.dto.BookingMessageRequest;
import com.b4code.backend.dto.AutoReplyRuleDto;
import com.b4code.backend.models.Booking;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.repository.AutoReplyRuleRepository;
import com.b4code.backend.service.BookingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/guest/bookings/{bookingId}/messages")
@RequiredArgsConstructor
public class GuestMessageController {

    private final BookingMessageService bookingMessageService;
    private final BookingRepository bookingRepository;
    private final AutoReplyRuleRepository autoReplyRuleRepository;

    @GetMapping
    public ResponseEntity<List<BookingMessageDto>> getMessages(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingMessageService.getMessagesForBooking(bookingId));
    }

    @GetMapping("/quick-requests")
    public ResponseEntity<List<AutoReplyRuleDto>> getQuickRequests(@PathVariable String bookingId) {
        Booking booking = null;
        try {
            Long id = Long.parseLong(bookingId);
            booking = bookingRepository.findById(id).orElseGet(() ->
                    bookingRepository.findByConfirmationCode(bookingId).orElse(null)
            );
        } catch (NumberFormatException e) {
            booking = bookingRepository.findByConfirmationCode(bookingId).orElse(null);
        }

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        List<AutoReplyRuleDto> activeRules = autoReplyRuleRepository.findByPropertyIdAndIsActiveTrue(booking.getProperty().getId())
                .stream()
                .map(rule -> AutoReplyRuleDto.builder()
                        .id(rule.getId())
                        .keyword(rule.getKeyword())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(activeRules);
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
