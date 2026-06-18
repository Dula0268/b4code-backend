package com.b4code.backend.service;

import com.b4code.backend.dto.BookingDto.*;
import com.b4code.backend.exceptions.ResourceNotFoundException;
import com.b4code.backend.exceptions.RoomNotAvailableException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Booking.BookingStatus;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.PromoCode;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10%

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final EmailService emailService;

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
                room.getId(), request.getCheckIn(), request.getCheckOut());
        if (overlap) {
            throw new RoomNotAvailableException(
                    "Room is not available for the selected dates");
        }

        // Re-calculate price server-side (never trust client-sent totals)
        PriceBreakdown price = getPrice(
                room.getId(), request.getCheckIn(), request.getCheckOut(), request.getPromoCode());

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
    // Send Confirmation Email (called by frontend
    // after PayHere redirect, since notify_url
    // cannot reach localhost in development)
    // ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public void sendReceiptEmail(String confirmationCode) {
        Booking booking = bookingRepository.findByConfirmationNumber(confirmationCode)
                .orElseThrow(() -> new com.b4code.backend.exceptions.ResourceNotFoundException(
                        "Booking not found: " + confirmationCode));

        if (booking.getPaymentMethod() != Booking.PaymentMethod.ONLINE_CARD) {
            log.info("[EMAIL] Skipping receipt email – booking {} is pay-at-property", confirmationCode);
            return;
        }

        String propertyName = booking.getRoom().getProperty().getName();
        String roomName     = booking.getRoom().getName();

        log.info("[EMAIL] Sending receipt email to {} for booking {}", booking.getGuestEmail(), confirmationCode);
        emailService.sendBookingConfirmationEmail(
                booking.getGuestEmail(),
                booking.getGuestName(),
                booking.getConfirmationNumber(),
                propertyName + " – " + roomName,
                booking.getCheckIn().toString(),
                booking.getCheckOut().toString(),
                booking.getTotalAmount().toString());
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
                booking.getId(), room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (overlap) {
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }

        PriceBreakdown newPrice = calculatePrice(room, request.getCheckInDate(), request.getCheckOutDate(),
                booking.getPromoCode(), false);
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
        booking.setPaymentMethod(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : booking.getPaymentMethod());

        Booking saved = bookingRepository.save(booking);

        return ModifyBookingResponse.builder()
                .booking(mapToResponse(saved))
                .previousTotalAmount(previousTotal)
                .newTotalAmount(newTotal)
                .refundAmount(difference.compareTo(BigDecimal.ZERO) < 0 ? difference.abs() : BigDecimal.ZERO)
                .additionalAmountDue(difference.compareTo(BigDecimal.ZERO) > 0 ? difference : BigDecimal.ZERO)
                .build();
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
            PromoCode code = promoCodeRepository.findByCodeIgnoreCase(promoCode)
                    .orElse(null);

            if (code != null && code.isValid()) {
                discountAmount = subtotal.multiply(code.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);
            }
        }

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = subtotalAfterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotalAfterDiscount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        return PriceBreakdown.builder()
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .nights((int) nights)
                .build();
    }

    private BookingResponse mapToResponse(Booking booking) {
        Property property = booking.getRoom().getProperty();
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .confirmationNumber(booking.getConfirmationNumber())
                .roomId(booking.getRoom().getId())
                .roomName(booking.getRoom().getName())
                .propertyId(property.getId())
                .propertyName(property.getName())
                .propertyAddress(property.getAddress())
                .propertyImage(property.getImageUrl() != null ? property.getImageUrl() : property.getImageSrc())
                .hostName(property.getHostName())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestCount(booking.getGuestCount())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .nights((int) ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut()))
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentMethod(booking.getPaymentMethod())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private Room findRoomOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }

    private String generateConfirmationNumber() {
        return "CONF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
