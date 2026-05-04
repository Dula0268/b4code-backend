package com.b4code.backend.modules.guest.controller;

import com.b4code.backend.modules.guest.dto.*;
import com.b4code.backend.modules.guest.entity.Booking;
import com.b4code.backend.modules.guest.entity.Message;
import com.b4code.backend.modules.guest.entity.Review;
import com.b4code.backend.modules.guest.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class GuestController {

    private final GuestService guestService;

    // ───────── PROPERTY ENDPOINTS ─────────

    /**
     * GET /api/guest/properties
     * Get all properties for browsing/searching
     */
    @GetMapping("/properties")
    public ResponseEntity<List<PropertyListingDto>> getAllProperties() {
        List<PropertyListingDto> properties = guestService.getAllProperties();
        return ResponseEntity.ok(properties);
    }

    /**
     * GET /api/guest/properties/{propertyId}
     * Get detailed info about a specific property
     */
    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<PropertyDetailDto> getPropertyDetail(@PathVariable Long propertyId) {
        PropertyDetailDto property = guestService.getPropertyDetail(propertyId);
        return ResponseEntity.ok(property);
    }

    // ───────── BOOKING ENDPOINTS ─────────

    /**
     * GET /api/guest/bookings/{guestId}
     * Get all bookings for a guest
     */
    @GetMapping("/bookings/{guestId}")
    public ResponseEntity<List<BookingDto>> getGuestBookings(@PathVariable Long guestId) {
        List<BookingDto> bookings = guestService.getGuestBookings(guestId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/guest/properties/{propertyId}/bookings
     * Get all bookings for a property
     */
    @GetMapping("/properties/{propertyId}/bookings")
    public ResponseEntity<List<BookingDto>> getPropertyBookings(@PathVariable Long propertyId) {
        List<BookingDto> bookings = guestService.getPropertyBookings(propertyId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * POST /api/guest/bookings
     * Create a new booking
     */
    @PostMapping("/bookings")
    public ResponseEntity<BookingDto> createBooking(@RequestBody Booking booking) {
        BookingDto newBooking = guestService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    // ───────── REVIEW ENDPOINTS ─────────

    /**
     * GET /api/guest/properties/{propertyId}/reviews
     * Get all reviews for a property
     */
    @GetMapping("/properties/{propertyId}/reviews")
    public ResponseEntity<List<ReviewDto>> getPropertyReviews(@PathVariable Long propertyId) {
        List<ReviewDto> reviews = guestService.getPropertyReviews(propertyId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/guest/reviews/{guestId}
     * Get all reviews by a guest
     */
    @GetMapping("/reviews/{guestId}")
    public ResponseEntity<List<ReviewDto>> getGuestReviews(@PathVariable Long guestId) {
        List<ReviewDto> reviews = guestService.getGuestReviews(guestId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * POST /api/guest/reviews
     * Create a new review
     */
    @PostMapping("/reviews")
    public ResponseEntity<ReviewDto> createReview(@RequestBody Review review) {
        ReviewDto newReview = guestService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReview);
    }

    // ───────── MESSAGE ENDPOINTS ─────────

    /**
     * GET /api/guest/properties/{propertyId}/messages
     * Get all messages for a property
     */
    @GetMapping("/properties/{propertyId}/messages")
    public ResponseEntity<List<MessageDto>> getPropertyMessages(@PathVariable Long propertyId) {
        List<MessageDto> messages = guestService.getPropertyMessages(propertyId);
        return ResponseEntity.ok(messages);
    }

    /**
     * GET /api/guest/messages
     * Get received messages for a user
     * Query param: receiverId
     */
    @GetMapping("/messages")
    public ResponseEntity<List<MessageDto>> getReceivedMessages(@RequestParam Long receiverId) {
        List<MessageDto> messages = guestService.getReceivedMessages(receiverId);
        return ResponseEntity.ok(messages);
    }

    /**
     * GET /api/guest/messages/conversation
     * Get conversation between two users
     * Query params: userId1, userId2
     */
    @GetMapping("/messages/conversation")
    public ResponseEntity<List<MessageDto>> getConversation(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        List<MessageDto> messages = guestService.getConversation(userId1, userId2);
        return ResponseEntity.ok(messages);
    }

    /**
     * POST /api/guest/messages
     * Send a message
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageDto> sendMessage(@RequestBody Message message) {
        MessageDto newMessage = guestService.sendMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(newMessage);
    }
}
