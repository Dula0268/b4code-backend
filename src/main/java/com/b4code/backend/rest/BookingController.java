package com.b4code.backend.rest;

import com.b4code.backend.dto.BookingDto.*;
import com.b4code.backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guest/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final com.b4code.backend.service.BookingReminderService bookingReminderService;

    @PostMapping("/trigger-reminders")
    public ResponseEntity<String> triggerReminders() {
        bookingReminderService.sendRemindersForUpcomingBookings();
        return ResponseEntity.ok("Reminders triggered");
    }

    /**
     * GET /api/guest/bookings/price-preview
     * Returns price breakdown before confirming.
     */
    @GetMapping("/price-preview")
    public ResponseEntity<PriceBreakdown> pricePreview(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false, defaultValue = "1") Integer roomQuantity,
            @RequestParam(required = false) List<String> promoCodes) {

        return ResponseEntity.ok(
                bookingService.getPrice(roomId, checkIn, checkOut, roomQuantity, promoCodes));
    }

    /**
     * POST /api/guest/bookings
     * Create a new booking.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {

        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/guest/bookings/confirmation/{confirmationNumber}
     * Retrieve booking by confirmation number (shown on confirmation screen).
     */
    @GetMapping("/confirmation/{confirmationNumber}")
    public ResponseEntity<BookingResponse> getByConfirmation(
            @PathVariable String confirmationNumber) {

        return ResponseEntity.ok(
                bookingService.getByConfirmationNumber(confirmationNumber));
    }

    /**
     * GET /api/guest/bookings/guest
     * Get all bookings for a guest by email.
     */
    @GetMapping("/guest")
    public ResponseEntity<List<BookingResponse>> getGuestBookings(
            @RequestParam String email) {

        return ResponseEntity.ok(bookingService.getGuestBookings(email));
    }

    /**
     * PATCH /api/guest/bookings/{id}/cancel
     * Cancel a booking.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequest request) {

        return ResponseEntity.ok(bookingService.cancelBooking(id, request));
    }

    /**
     * PATCH /api/guest/bookings/{id}/complete
     * Mark a booking as completed.
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long id) {

        return ResponseEntity.ok(bookingService.completeBooking(id));
    }

    /**
     * PUT /api/guest/bookings/{id}
     * Modify an existing booking.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ModifyBookingResponse> modifyBooking(
            @PathVariable Long id,
            @Valid @RequestBody ModifyBookingRequest request) {

        return ResponseEntity.ok(bookingService.modifyBooking(id, request));
    }

    /**
     * POST /api/guest/bookings/{confirmationCode}/send-receipt
     * Triggered by the frontend after PayHere redirects back to the confirmation page.
     * Sends the booking confirmation email for ONLINE_CARD payments.
     * Safe to call multiple times — idempotent by design.
     */
    @PostMapping("/{confirmationCode}/send-receipt")
    public ResponseEntity<java.util.Map<String, String>> sendReceipt(
            @PathVariable String confirmationCode) {
        bookingService.sendReceiptEmail(confirmationCode);
        return ResponseEntity.ok(java.util.Map.of("message", "Receipt email sent successfully"));
    }

    @PostMapping("/complaint")
    public ResponseEntity<com.b4code.backend.dto.DisputeDto> submitComplaint(
            @Valid @RequestBody com.b4code.backend.dto.ComplaintRequestDto request) {
        return ResponseEntity.ok(bookingService.createComplaint(request));
    }

    @ExceptionHandler({ com.b4code.backend.exceptions.RoomNotAvailableException.class, IllegalArgumentException.class,
            IllegalStateException.class })
    public ResponseEntity<java.util.Map<String, String>> handleBookingValidations(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                "error", "Bad Request",
                "message", ex.getMessage()));
    }
}
