package com.b4code.backend.service;

import com.b4code.backend.dto.BookingDto.*;
import com.b4code.backend.exceptions.ResourceNotFoundException;
import com.b4code.backend.exceptions.RoomNotAvailableException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.PromoCode;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10%

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final PromoCodeRepository promoCodeRepository;

    // ──────────────────────────────────────────
    // Price Preview (called before confirming)
    // ──────────────────────────────────────────
    public PriceBreakdown getPrice(Long roomId, LocalDate checkIn, LocalDate checkOut, String promoCode) {
        Room room = findRoomOrThrow(roomId);
        return calculatePrice(room, checkIn, checkOut, promoCode, true);
    }

    // ──────────────────────────────────────────
    // Create Booking
    // ──────────────────────────────────────────
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        Room room = findRoomOrThrow(request.getRoomId());
        
        Property property = propertyRepository.findById(request.getPropertyId())
            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        boolean overlap = bookingRepository.existsOverlappingBooking(
                room.getId(), request.getCheckIn(), request.getCheckOut());
        if (overlap) {
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }

        PriceBreakdown price = getPrice(
                room.getId(), request.getCheckIn(), request.getCheckOut(), request.getPromoCode());

        Booking booking = Booking.builder()
                .room(room)
                .property(property)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .adults(request.getAdults())
                .children(request.getChildren())
                .totalAmount(price.getTotalAmount())
                .taxAmount(price.getTaxAmount())
                .promoCode(request.getPromoCode())
                .paymentMethod(request.getPaymentMethod())
                .build();

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    // ──────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────

    private PriceBreakdown calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut, String promoCode,
            boolean isPreview) {
        BigDecimal pricePerNight = room.getPricePerNight();

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new IllegalArgumentException("Stay must be at least 1 night");
        }

        BigDecimal subtotal = pricePerNight.multiply(BigDecimal.valueOf(nights));

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (promoCode != null && !promoCode.isBlank()) {
            PromoCode code = promoCodeRepository.findByCodeIgnoreCase(promoCode).orElse(null);
            if (code != null && code.isValid()) {
                discountAmount = subtotal.multiply(code.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = subtotalAfterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotalAfterDiscount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        return PriceBreakdown.builder()
                .roomId(room.getId())
                .nights((int) nights)
                .pricePerNight(pricePerNight)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .promoApplied(promoCode)
                .build();
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .propertyId(booking.getProperty().getId())
                .reviewId(booking.getReview() != null ? booking.getReview().getId() : null)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .promoCode(booking.getPromoCode())
                .paymentMethod(booking.getPaymentMethod())
                .taxAmount(booking.getTaxAmount())
                .totalAmount(booking.getTotalAmount())
                .build();
    }

    private Room findRoomOrThrow(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }
}
