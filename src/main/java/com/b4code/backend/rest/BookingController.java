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
                bookingService.getPrice(roomId, checkIn, checkOut, promoCode));
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

    @ExceptionHandler({ com.b4code.backend.exceptions.RoomNotAvailableException.class, IllegalArgumentException.class,
            IllegalStateException.class })
    public ResponseEntity<java.util.Map<String, String>> handleBookingValidations(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                "error", "Bad Request",
                "message", ex.getMessage()));
    }
}
