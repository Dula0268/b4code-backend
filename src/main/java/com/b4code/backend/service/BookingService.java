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
import com.b4code.backend.dao.PropertyRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10%

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
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

        String generatedCode = "B4C-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal finalTotal = request.getTotalAmount() != null 
                ? request.getTotalAmount() 
                : price.getTotalAmount();
        
        // If frontend provided the total, just calculate a rough tax estimate for the DB record
        BigDecimal finalTax = request.getTotalAmount() != null
                ? finalTotal.divide(BigDecimal.valueOf(11), 2, java.math.RoundingMode.HALF_UP)
                : price.getTaxAmount();

        Booking booking = Booking.builder()
                .room(room)
                .property(property)
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail())
                .confirmationCode(generatedCode)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .adults(request.getAdults())
                .children(request.getChildren())
                .totalAmount(finalTotal)
                .taxAmount(finalTax)
                .promoCode(request.getPromoCode())
                .paymentMethod(request.getPaymentMethod())
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
        Booking booking = bookingRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new com.b4code.backend.exceptions.ResourceNotFoundException(
                        "Booking not found: " + confirmationCode));

        if (booking.getPaymentMethod() != Booking.PaymentMethod.ONLINE_CARD) {
            log.info("[EMAIL] Skipping receipt email – booking {} is pay-at-property", confirmationCode);
            return;
        }

        String propertyName = booking.getRoom().getProperty().getName();
        String roomName     = booking.getRoom().getRoomType().name();

        log.info("[EMAIL] Sending receipt email to {} for booking {}", booking.getGuestEmail(), confirmationCode);
        emailService.sendBookingConfirmationEmail(
                booking.getGuestEmail(),
                booking.getGuestName(),
                booking.getConfirmationCode(),
                propertyName + " – " + roomName,
                booking.getCheckIn().toString(),
                booking.getCheckOut().toString(),
                booking.getTotalAmount().toString());
    }

    // ──────────────────────────────────────────
    // Get by Confirmation Number
    // ──────────────────────────────────────────
    public BookingResponse getByConfirmationNumber(String confirmationNumber) {
        Booking booking = bookingRepository.findByConfirmationCode(confirmationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + confirmationNumber));
        return mapToResponse(booking);
    }

    // ──────────────────────────────────────────
    // Get by ID
    // ──────────────────────────────────────────
    public BookingResponse getById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
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
        BigDecimal previousTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        
        BigDecimal newTotal = request.getTotalAmount() != null 
                ? request.getTotalAmount() 
                : newPrice.getTotalAmount();
                
        BigDecimal newTax = request.getTotalAmount() != null
                ? newTotal.divide(BigDecimal.valueOf(11), 2, RoundingMode.HALF_UP)
                : newPrice.getTaxAmount();
                
        BigDecimal difference = newTotal.subtract(previousTotal);

        booking.setRoom(room);
        booking.setCheckIn(request.getCheckInDate());
        booking.setCheckOut(request.getCheckOutDate());
        booking.setGuestCount(request.getGuests());
        booking.setTotalAmount(newTotal);
        booking.setTaxAmount(newTax);
        booking.setDiscountAmount(newPrice.getDiscountAmount() != null ? newPrice.getDiscountAmount() : BigDecimal.ZERO);
        
        Booking.PaymentMethod fallbackMethod = booking.getPaymentMethod() != null ? booking.getPaymentMethod() : Booking.PaymentMethod.ONLINE_CARD;
        booking.setPaymentMethod(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : fallbackMethod);

        // Fix legacy nulls for existing records to prevent Hibernate PropertyValueException
        if (booking.getGuestEmail() == null) booking.setGuestEmail("admin@example.com");
        if (booking.getGuestName() == null) booking.setGuestName("Guest");
        if (booking.getAdults() == null) booking.setAdults(1);
        if (booking.getChildren() == null) booking.setChildren(0);

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
                .discountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .promoApplied(promoCode)
                .build();
    }

    private BookingResponse mapToResponse(Booking booking) {
        int guestCount = booking.getGuestCount() != null ? booking.getGuestCount() : (booking.getAdults() + booking.getChildren());
        int nights = (int) ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());

        String primaryImage = null;
        if (booking.getProperty() != null) {
            if (booking.getProperty().getImages() != null && !booking.getProperty().getImages().isEmpty()) {
                primaryImage = booking.getProperty().getImages().get(0).getUrl();
            } else if (booking.getProperty().getImageSrc() != null && !booking.getProperty().getImageSrc().trim().isEmpty()) {
                primaryImage = booking.getProperty().getImageSrc();
            } else if (booking.getProperty().getImageUrl() != null && !booking.getProperty().getImageUrl().trim().isEmpty()) {
                primaryImage = booking.getProperty().getImageUrl();
            }
        }

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .confirmationNumber(booking.getConfirmationCode())
                .roomId(booking.getRoom().getId())
                .propertyId(booking.getProperty().getId())
                .propertyName(booking.getProperty().getName())
                .propertyAddress(booking.getProperty().getAddress())
                .propertyImage(primaryImage)
                .roomName(booking.getRoom().getRoomType().name())
                .reviewId(booking.getReview() != null ? booking.getReview().getId() : null)
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestCount(guestCount)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .nights(nights)
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .promoCode(booking.getPromoCode())
                .paymentMethod(booking.getPaymentMethod())
                .taxAmount(booking.getTaxAmount())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt().toString())
                .build();
    }

    private Room findRoomOrThrow(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }
}
