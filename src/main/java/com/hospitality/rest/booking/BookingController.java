package com.hospitality.rest.booking;

import com.hospitality.dto.booking.BookingDto.*;
import com.hospitality.service.BookingService;
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
}