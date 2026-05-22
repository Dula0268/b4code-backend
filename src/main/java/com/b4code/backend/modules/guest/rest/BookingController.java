package com.b4code.backend.modules.guest.rest;

import com.b4code.backend.modules.guest.dto.BookingDto.*;
import com.b4code.backend.modules.guest.service.BookingService;
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
@CrossOrigin(origins = "*") // adjust for prod
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
    * GET /api/guest/bookings/price-preview
     * Returns price breakdown before confirming.
     */
    @GetMapping("/price-preview")
    public ResponseEntity<PriceBreakdown> pricePreview(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) String promoCode) {

        return ResponseEntity.ok(
            bookingService.getPrice(roomId, checkIn, checkOut, promoCode)
        );
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
            bookingService.getByConfirmationNumber(confirmationNumber)
        );
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

    @ExceptionHandler({com.b4code.backend.modules.guest.exceptions.RoomNotAvailableException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<java.util.Map<String, String>> handleBookingValidations(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
            "error", "Bad Request",
            "message", ex.getMessage()
        ));
    }
}
