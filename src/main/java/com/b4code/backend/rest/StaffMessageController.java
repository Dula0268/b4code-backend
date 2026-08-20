package com.b4code.backend.rest;

import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.ActiveConversationDto;
import com.b4code.backend.dto.BookingMessageDto;
import com.b4code.backend.dto.BookingMessageRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.service.BookingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/staff/messages")
@RequiredArgsConstructor
public class StaffMessageController {

    private static final Set<String> BOOKING_INBOX_STAFF_ROLES = Set.of("Property Staff", "Staff Admin");

    private final BookingMessageService bookingMessageService;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/property/{propertyId}/conversations")
    public ResponseEntity<List<ActiveConversationDto>> getConversations(@PathVariable Long propertyId, java.security.Principal principal) {
        assertBookingInboxAccess(principal.getName());
        return ResponseEntity.ok(bookingMessageService.getConversationsForProperty(propertyId));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingMessageDto>> getMessages(@PathVariable String bookingId, java.security.Principal principal) {
        assertBookingInboxAccess(principal.getName());
        return ResponseEntity.ok(bookingMessageService.getMessagesForBooking(bookingId));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<BookingMessageDto> sendMessage(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingMessageRequest request,
            java.security.Principal principal) {
        assertBookingInboxAccess(principal.getName());

        BookingMessageDto message = bookingMessageService.sendMessage(
                bookingId,
                principal.getName(),
                "STAFF",
                request.getContent()
        );
        return ResponseEntity.ok(message);
    }

    /**
     * Only staff whose staffRole is "Property Staff" or "Staff Admin" may access the
     * booking-guest inbox ("Kitchen Staff" must be rejected). OWNER/ADMIN platform roles
     * (Spring role, not staffRole) bypass this check entirely - they're not staff accounts.
     * Mirror image of {@code OrderMessageService#assertKitchenStaffAccess}.
     */
    private void assertBookingInboxAccess(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (user.getRole() == UserRole.STAFF) {
            if (user.getStaffRole() == null || !BOOKING_INBOX_STAFF_ROLES.contains(user.getStaffRole())) {
                throw new CustomException("This inbox is not available for your staff role", HttpStatus.FORBIDDEN);
            }
        }
    }
}
