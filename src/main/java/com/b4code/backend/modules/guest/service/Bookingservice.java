package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.BookingDto.*;
import com.b4code.backend.modules.guest.exceptions.ResourceNotFoundException;
import com.b4code.backend.modules.guest.exceptions.RoomNotAvailableException;
import com.b4code.backend.modules.guest.models.Booking;
import com.b4code.backend.modules.guest.models.Booking.BookingStatus;
import com.b4code.backend.modules.guest.models.Room;
import com.b4code.backend.modules.guest.dao.BookingRepository;
import com.b4code.backend.modules.guest.dao.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final BigDecimal TAX_RATE     = new BigDecimal("0.10"); // 10%
    private static final BigDecimal PROMO_RATE   = new BigDecimal("0.10"); // 10% off for demo

    private final BookingRepository bookingRepository;
    private final RoomRepository    roomRepository;

    // ──────────────────────────────────────────
    // Price Preview (called before confirming)
    // ──────────────────────────────────────────
    public PriceBreakdown getPrice(Long roomId, LocalDate checkIn, LocalDate checkOut, String promoCode) {

        Room room = findRoomOrThrow(roomId);

        long nights      = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal nNights = BigDecimal.valueOf(nights);
        BigDecimal subtotal = room.getPricePerNight().multiply(nNights);

        // Apply promo if provided (simple flat 10% discount for demo)
        BigDecimal discount = BigDecimal.ZERO;
        String promoApplied = null;
        if (promoCode != null && !promoCode.isBlank()) {
            discount = subtotal.multiply(PROMO_RATE).setScale(2, RoundingMode.HALF_UP);
            promoApplied = promoCode.toUpperCase();
        }

        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal tax   = afterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(tax);

        return PriceBreakdown.builder()
            .roomId(roomId)
            .roomName(room.getName())
            .nights((int) nights)
            .pricePerNight(room.getPricePerNight())
            .subtotal(subtotal)
            .discountAmount(discount)
            .taxAmount(tax)
            .totalAmount(total)
            .promoApplied(promoApplied)
            .build();
    }

    // ──────────────────────────────────────────
    // Create Booking
    // ──────────────────────────────────────────
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        Room room = findRoomOrThrow(request.getRoomId());

        // Validate dates
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        // Check availability (prevents overbooking)
        boolean overlap = bookingRepository.existsOverlappingBooking(
            room.getId(), request.getCheckIn(), request.getCheckOut()
        );
        if (overlap) {
            throw new RoomNotAvailableException(
                "Room is not available for the selected dates"
            );
        }

        // Re-calculate price server-side (never trust client-sent totals)
        PriceBreakdown price = getPrice(
            room.getId(), request.getCheckIn(), request.getCheckOut(), request.getPromoCode()
        );

        Booking booking = Booking.builder()
            .room(room)
            .guestName(request.getGuestName())
            .guestEmail(request.getGuestEmail())
            .guestPhone(request.getGuestPhone())
            .checkIn(request.getCheckIn())
            .checkOut(request.getCheckOut())
            .guestCount(request.getGuestCount())
            .totalAmount(price.getTotalAmount())
            .taxAmount(price.getTaxAmount())
            .promoCode(request.getPromoCode())
            .discountAmount(price.getDiscountAmount())
            .status(BookingStatus.CONFIRMED)
            .paymentMethod(request.getPaymentMethod())
            .confirmationNumber(generateConfirmationNumber())
            .build();

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    // ──────────────────────────────────────────
    // Get by Confirmation Number
    // ──────────────────────────────────────────
    public BookingResponse getByConfirmationNumber(String confirmationNumber) {
        Booking booking = bookingRepository.findByConfirmationNumber(confirmationNumber)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking not found: " + confirmationNumber));
        return mapToResponse(booking);
    }

    // ──────────────────────────────────────────
    // Get all bookings for a guest (by email)
    // ──────────────────────────────────────────
    public List<BookingResponse> getGuestBookings(String email) {
        return bookingRepository.findByGuestEmail(email)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────
    // Cancel Booking
    // ──────────────────────────────────────────
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, CancelBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return mapToResponse(bookingRepository.save(booking));
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    private Room findRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
    }

    private String generateConfirmationNumber() {
        return "HB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse mapToResponse(Booking b) {
        Room room = b.getRoom();
        long nights = ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut());

        return BookingResponse.builder()
            .bookingId(b.getId())
            .propertyId(room.getProperty().getId())
            .roomId(room.getId())
            .confirmationNumber(b.getConfirmationNumber())
            .guestName(b.getGuestName())
            .guestEmail(b.getGuestEmail())
            .propertyName(room.getProperty().getName())
            .propertyAddress(room.getProperty().getAddress())
            .roomName(room.getName())
            .checkIn(b.getCheckIn())
            .checkOut(b.getCheckOut())
            .nights((int) nights)
            .guestCount(b.getGuestCount())
            .totalAmount(b.getTotalAmount())
            .status(b.getStatus())
            .paymentMethod(b.getPaymentMethod())
            .createdAt(b.getCreatedAt())
            .build();
    }
}
