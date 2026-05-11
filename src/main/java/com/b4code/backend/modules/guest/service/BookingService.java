package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.BookingDto.*;
import com.b4code.backend.modules.guest.exceptions.ResourceNotFoundException;
import com.b4code.backend.modules.guest.exceptions.RoomNotAvailableException;
import com.b4code.backend.modules.guest.models.Booking;
import com.b4code.backend.modules.guest.models.Booking.BookingStatus;
import com.b4code.backend.modules.guest.models.Room;
import com.b4code.backend.modules.guest.models.PromoCode;
import com.b4code.backend.modules.guest.dao.BookingRepository;
import com.b4code.backend.modules.guest.dao.RoomRepository;
import com.b4code.backend.modules.guest.dao.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final BigDecimal TAX_RATE     = new BigDecimal("0.10"); // 10%

    private final BookingRepository bookingRepository;
    private final RoomRepository    roomRepository;
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
        booking.setCancellationReason(request.getReason());
        return mapToResponse(bookingRepository.save(booking));
    }

    // ──────────────────────────────────────────
    // Complete Booking
    // ──────────────────────────────────────────
    @Transactional
    public BookingResponse completeBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            return mapToResponse(booking);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled bookings cannot be completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return mapToResponse(bookingRepository.save(booking));
    }

    // ──────────────────────────────────────────
    // Modify Booking
    // ──────────────────────────────────────────
    @Transactional
    public ModifyBookingResponse modifyBooking(Long bookingId, ModifyBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled bookings cannot be modified");
        }

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + request.getRoomId()));

        if (!room.getProperty().getId().equals(request.getPropertyId())) {
            throw new IllegalArgumentException("Room does not belong to the selected property");
        }

        boolean overlap = bookingRepository.existsOverlappingBookingExcludingId(
            booking.getId(), room.getId(), request.getCheckInDate(), request.getCheckOutDate()
        );
        if (overlap) {
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }

        PriceBreakdown newPrice = calculatePrice(room, request.getCheckInDate(), request.getCheckOutDate(), booking.getPromoCode(), false);
        BigDecimal previousTotal = booking.getTotalAmount();
        BigDecimal newTotal = newPrice.getTotalAmount();
        BigDecimal difference = newTotal.subtract(previousTotal);

        booking.setRoom(room);
        booking.setCheckIn(request.getCheckInDate());
        booking.setCheckOut(request.getCheckOutDate());
        booking.setGuestCount(request.getGuests());
        booking.setTotalAmount(newTotal);
        booking.setTaxAmount(newPrice.getTaxAmount());
        booking.setDiscountAmount(newPrice.getDiscountAmount());
        booking.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : booking.getPaymentMethod());

        Booking saved = bookingRepository.save(booking);

        return ModifyBookingResponse.builder()
            .booking(mapToResponse(saved))
            .previousTotalAmount(previousTotal)
            .newTotalAmount(newTotal)
            .refundAmount(difference.signum() < 0 ? difference.abs() : BigDecimal.ZERO)
            .additionalAmountDue(difference.signum() > 0 ? difference : BigDecimal.ZERO)
            .build();
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    private Room findRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
    }

    private PriceBreakdown calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut, String promoCode, boolean incrementPromoUsage) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal nNights = BigDecimal.valueOf(nights);
        BigDecimal subtotal = room.getPricePerNight().multiply(nNights);

        BigDecimal totalDiscountPercent = BigDecimal.ZERO;
        List<String> appliedCodes = new ArrayList<>();

        if (promoCode != null && !promoCode.isBlank()) {
            // Split by comma to support multiple codes
            String[] codes = promoCode.split(",");
            for (String code : codes) {
                String trimmedCode = code.trim();
                if (trimmedCode.isEmpty()) continue;

                // Validate against database (only codes from seeder/DB will work)
                PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(trimmedCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid promo code: " + trimmedCode));

                // Property restriction check
                if (promo.getPropertyId() != null && !promo.getPropertyId().equals(room.getProperty().getId())) {
                    throw new IllegalArgumentException("Promo code '" + trimmedCode + "' is not valid for this property");
                }

                // Validity check (expiry, usage limit, active status)
                if (!promo.isValid()) {
                    throw new IllegalArgumentException("Promo code '" + trimmedCode + "' has expired or reached its usage limit");
                }

                totalDiscountPercent = totalDiscountPercent.add(promo.getDiscountPercent());
                appliedCodes.add(promo.getCode().toUpperCase());

                if (incrementPromoUsage) {
                    promo.setCurrentUses(promo.getCurrentUses() + 1);
                    promoCodeRepository.save(promo);
                }
            }
        }

        // Cap total discount at 100%
        if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) {
            totalDiscountPercent = new BigDecimal("100");
        }

        BigDecimal discountRate = totalDiscountPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discount = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        String promoApplied = appliedCodes.isEmpty() ? null : String.join(", ", appliedCodes);

        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal tax = afterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(tax);

        return PriceBreakdown.builder()
            .roomId(room.getId())
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
            .cancellationReason(b.getCancellationReason())
            .paymentMethod(b.getPaymentMethod())
            .propertyImage(b.getRoom().getProperty().getImageSrc())
            .hostName(b.getRoom().getProperty().getHostName())
            .createdAt(b.getCreatedAt())
            .build();
    }
}
