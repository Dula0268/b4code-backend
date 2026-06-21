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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;
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
    private final com.b4code.backend.dao.PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final com.b4code.backend.dao.DisputeRepository disputeRepository;
    private final com.b4code.backend.dao.UserRepository userRepository;

    // ──────────────────────────────────────────
    // Price Preview (called before confirming)
    // ──────────────────────────────────────────
    public PriceBreakdown getPrice(Long roomId, LocalDate checkIn, LocalDate checkOut, Integer roomQuantity, List<String> promoCodes) {
        Room room = findRoomOrThrow(roomId);
        return calculatePrice(room, checkIn, checkOut, roomQuantity, promoCodes, true);
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

        int bookedQuantity = bookingRepository.getBookedQuantityForDates(
                room.getId(), request.getCheckIn(), request.getCheckOut());
        if (room.getInventory() - bookedQuantity < request.getRoomQuantity()) {
            throw new RoomNotAvailableException("Room is not available for the selected dates with the requested quantity");
        }

        PriceBreakdown price = getPrice(
                room.getId(), request.getCheckIn(), request.getCheckOut(), request.getRoomQuantity(), request.getPromoCodes());

        Booking booking = Booking.builder()
                .confirmationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail())
                .nicNumber(request.getNicNumber())
                .room(room)
                .roomQuantity(request.getRoomQuantity())
                .property(property)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .adults(request.getAdults())
                .children(request.getChildren())
                .totalAmount(price.getTotalAmount())
                .taxAmount(price.getTaxAmount())
                .promoCode(request.getPromoCodes() != null && !request.getPromoCodes().isEmpty() ? String.join(",", request.getPromoCodes()) : null)
                .paymentMethod(request.getPaymentMethod())
                .build();

        Booking saved = bookingRepository.save(booking);

        if (request.getPromoCodes() != null && !request.getPromoCodes().isEmpty()) {
            for (String codeStr : request.getPromoCodes()) {
                PromoCode code = promoCodeRepository.findByCodeIgnoreCase(codeStr).orElse(null);
                if (code != null && code.isValid()) {
                    code.setCurrentUses(code.getCurrentUses() + 1);
                    promoCodeRepository.save(code);
                }
            }
        }

        // Send confirmation email with itinerary
        try {
            String propertyName = saved.getProperty().getName();
            String roomName = saved.getRoom().getRoomType().name();
            emailService.sendBookingConfirmationEmail(
                    saved.getGuestEmail(),
                    saved.getGuestName(),
                    saved.getConfirmationCode(),
                    propertyName + " – " + roomName,
                    saved.getCheckIn().toString(),
                    saved.getCheckOut().toString(),
                    saved.getTotalAmount().toString());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}", saved.getGuestEmail(), e);
        }

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
    // Get all bookings for a guest (by email)
    // ──────────────────────────────────────────
    public List<BookingResponse> getGuestBookings(String email) {
        return bookingRepository.findByGuestEmailOrderByCreatedAtDesc(email)
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
        Booking saved = bookingRepository.save(booking);

        // Calculate refund amount
        Property property = booking.getProperty();
        BigDecimal totalPaid = booking.getPaymentMethod() == Booking.PaymentMethod.ONLINE_CARD 
            ? booking.getTotalAmount() 
            : BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;

        if (property.getFreeCancellation() != null && !property.getFreeCancellation()) {
            // 20% cancellation fee if not free
            fee = totalPaid.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal refundAmount = totalPaid.subtract(fee);

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            com.b4code.backend.models.Dispute dispute = new com.b4code.backend.models.Dispute();
            dispute.setDisputeId(UUID.randomUUID().toString());
            dispute.setBooking(saved);
            dispute.setProperty(property);
            
            // Link guest user if exists
            if (userRepository != null) {
                userRepository.findByEmailAndDeletedFalse(saved.getGuestEmail())
                    .ifPresent(dispute::setGuest);
            }

            dispute.setAmount(refundAmount);
            String reasonText = request.getReason() != null && !request.getReason().isBlank() 
                ? " - Reason: " + request.getReason() 
                : "";
            dispute.setReason("Booking cancellation refund request for " + saved.getConfirmationCode() + reasonText);
            dispute.setStatus(com.b4code.backend.models.enums.DisputeStatus.OPEN);
            
            if (disputeRepository != null) {
                disputeRepository.save(dispute);
            }
        }

        // We skip automatic payment gateway refund (paymentService.refundPayment) 
        // to let admin handle it via Dispute dashboard instead.

        return mapToResponse(saved);
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

        int bookedQuantity = bookingRepository.getBookedQuantityForDatesExcludingId(
                booking.getId(), room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (room.getInventory() - bookedQuantity < booking.getRoomQuantity()) {
            throw new RoomNotAvailableException("Room is not available for the selected dates with the current quantity");
        }

        List<String> currentPromoCodes = booking.getPromoCode() != null ? Arrays.asList(booking.getPromoCode().split(",")) : null;
        PriceBreakdown newPrice = calculatePrice(room, request.getCheckInDate(), request.getCheckOutDate(),
                booking.getRoomQuantity(), currentPromoCodes, false);
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

        boolean isPaidOnline = saved.getPaymentMethod() == Booking.PaymentMethod.ONLINE_CARD;

        if (difference.compareTo(BigDecimal.ZERO) < 0 && isPaidOnline) {
            com.b4code.backend.models.Dispute dispute = new com.b4code.backend.models.Dispute();
            dispute.setDisputeId(UUID.randomUUID().toString());
            dispute.setBooking(saved);
            dispute.setProperty(saved.getProperty());
            
            // Link guest user if exists
            userRepository.findByEmailAndDeletedFalse(saved.getGuestEmail())
                .ifPresent(dispute::setGuest);

            dispute.setAmount(difference.abs());
            dispute.setReason("Booking modification refund request for " + saved.getConfirmationCode());
            dispute.setStatus(com.b4code.backend.models.enums.DisputeStatus.OPEN);
            
            disputeRepository.save(dispute);
        }

        return ModifyBookingResponse.builder()
                .booking(mapToResponse(saved))
                .previousTotalAmount(previousTotal)
                .newTotalAmount(newTotal)
                .refundAmount((isPaidOnline && difference.compareTo(BigDecimal.ZERO) < 0) ? difference.abs() : BigDecimal.ZERO)
                .additionalAmountDue((isPaidOnline && difference.compareTo(BigDecimal.ZERO) > 0) ? difference : BigDecimal.ZERO)
                .build();
    }

    // ──────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────

    private PriceBreakdown calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut, Integer roomQuantity, List<String> promoCodes,
            boolean isPreview) {
        BigDecimal pricePerNight = room.getPricePerNight();

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new IllegalArgumentException("Stay must be at least 1 night");
        }

        BigDecimal subtotal = pricePerNight.multiply(BigDecimal.valueOf(nights)).multiply(BigDecimal.valueOf(roomQuantity));

        BigDecimal discountAmount = BigDecimal.ZERO;
        List<String> validPromos = new ArrayList<>();
        if (promoCodes != null && !promoCodes.isEmpty()) {
            BigDecimal totalDiscountPercent = BigDecimal.ZERO;
            for (String pCode : promoCodes) {
                if (pCode == null || pCode.isBlank()) continue;
                PromoCode code = promoCodeRepository.findByCodeIgnoreCase(pCode.trim())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid promo code: " + pCode));
                        
                if (!code.isValid()) {
                    throw new IllegalArgumentException("Promo code is expired or usage limit reached: " + pCode);
                }
                
                if (code.getPropertyId() != null && !code.getPropertyId().equals(room.getProperty().getId())) {
                    throw new IllegalArgumentException("Promo code is not valid for this property: " + pCode);
                }

                totalDiscountPercent = totalDiscountPercent.add(code.getDiscountPercent());
                validPromos.add(code.getCode());
            }
            discountAmount = subtotal.multiply(totalDiscountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = subtotalAfterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotalAfterDiscount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        return PriceBreakdown.builder()
                .roomId(room.getId())
                .roomQuantity(roomQuantity)
                .nights((int) nights)
                .pricePerNight(pricePerNight)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .promosApplied(validPromos.isEmpty() ? null : validPromos)
                .build();
    }

    private BookingResponse mapToResponse(Booking booking) {
        String address = booking.getProperty().getAddressLine1() != null ? booking.getProperty().getAddressLine1() : booking.getProperty().getAddress();
        if (booking.getProperty().getCity() != null) {
            address = address != null ? address + ", " + booking.getProperty().getCity() : booking.getProperty().getCity();
        }

        String displayStatus = booking.getStatus().name();
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED && booking.getCheckOut().isBefore(LocalDate.now())) {
            displayStatus = "COMPLETED";
        }

        BookingResponse response = BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .roomName(booking.getRoom().getRoomType().name())
                .roomQuantity(booking.getRoomQuantity())
                .propertyId(booking.getProperty().getId())
                .propertyName(booking.getProperty().getName())
                .propertyAddress(address)
                .propertyImage(
                    booking.getProperty().getImages() != null && !booking.getProperty().getImages().isEmpty()
                        ? booking.getProperty().getImages().stream()
                            .filter(img -> com.b4code.backend.models.ImageType.PROPERTY.equals(img.getType()))
                            .map(com.b4code.backend.models.Image::getUrl)
                            .findFirst()
                            .orElse(booking.getProperty().getImages().get(0).getUrl())
                        : "/images/placeholder-property.jpg"
                )
                .reviewId(booking.getReview() != null ? booking.getReview().getId() : null)
                .confirmationCode(booking.getConfirmationCode())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .adults(booking.getAdults())
                .promoCodes(booking.getPromoCode() != null ? Arrays.asList(booking.getPromoCode().split(",")) : null)
                .paymentMethod(booking.getPaymentMethod())
                .status(displayStatus)
                .taxAmount(booking.getTaxAmount())
                .totalAmount(booking.getTotalAmount())
                .build();
                
        if (booking.getId() != null && disputeRepository != null) {
            disputeRepository.findTopByBookingIdOrderByOpenedAtDesc(booking.getId())
                .ifPresent(d -> {
                    response.setDisputeStatus(d.getStatus().name());
                    response.setDisputeAmount(d.getAmount());
                });
        }
        
        return response;
    }

    private Room findRoomOrThrow(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }
}
