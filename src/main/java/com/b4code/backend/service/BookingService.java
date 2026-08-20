package com.b4code.backend.service;

import com.b4code.backend.dto.BookingDto.*;
import com.b4code.backend.exceptions.ResourceNotFoundException;
import com.b4code.backend.exceptions.RoomNotAvailableException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Booking.BookingStatus;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.RoomType;
import com.b4code.backend.models.PromoCode;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomTypeRepository;
import com.b4code.backend.dao.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
@Transactional(readOnly = true)
public class BookingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.00"); // 0% tax for all properties

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final EmailService emailService;
    private final com.b4code.backend.dao.PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final com.b4code.backend.dao.DisputeRepository disputeRepository;
    private final com.b4code.backend.dao.RefundRepository refundRepository;
    private final com.b4code.backend.dao.UserRepository userRepository;
    private final com.b4code.backend.dao.RoomDateInventoryRepository roomDateInventoryRepository;
    private final NotificationService notificationService;
    private final BookingSseService bookingSseService;
    private final AdminNotificationService adminNotificationService;
    private final com.b4code.backend.dao.ReviewRepository reviewRepository;
    private final com.b4code.backend.dao.TransactionRepository transactionRepository;

    // ──────────────────────────────────────────
    // Price Preview (called before confirming)
    // ──────────────────────────────────────────
    public PriceBreakdown getPrice(Long roomId, LocalDate checkIn, LocalDate checkOut, Integer roomQuantity, List<String> promoCodes) {
        RoomType roomType = findRoomOrThrow(roomId);
        return calculatePrice(roomType, checkIn, checkOut, roomQuantity, promoCodes, true);
    }

    // ──────────────────────────────────────────
    // Create Booking
    // ──────────────────────────────────────────
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        RoomType roomType = findRoomOrThrow(request.getRoomId());

        Property property = propertyRepository.findById(request.getPropertyId())
            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        List<com.b4code.backend.models.RoomDateInventory> dailyInventories = roomDateInventoryRepository.findByRoomTypeIdAndDates(
                roomType.getId(), request.getCheckIn(), request.getCheckOut());
        
        int peakBooked = dailyInventories.stream()
                .mapToInt(com.b4code.backend.models.RoomDateInventory::getBookedQuantity)
                .max().orElse(0);

        if (roomType.getInventory() - peakBooked < request.getRoomQuantity()) {
            throw new RoomNotAvailableException("Room is not available for the selected dates with the requested quantity");
        }

        PriceBreakdown price = getPrice(
                roomType.getId(), request.getCheckIn(), request.getCheckOut(), request.getRoomQuantity(), request.getPromoCodes());

        Booking booking = Booking.builder()
                .confirmationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail())
                .nicNumber(request.getNicNumber())
                .roomType(roomType)
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
                .status(request.getPaymentMethod() == Booking.PaymentMethod.ONLINE_CARD ? Booking.BookingStatus.PENDING : Booking.BookingStatus.CONFIRMED)
                .build();

        Booking saved = bookingRepository.save(booking);

        // Update daily inventory
        updateInventory(roomType, request.getCheckIn(), request.getCheckOut(), request.getRoomQuantity());

        if (request.getPromoCodes() != null && !request.getPromoCodes().isEmpty()) {
            for (String codeStr : request.getPromoCodes()) {
                PromoCode code = promoCodeRepository.findByCodeIgnoreCase(codeStr).orElse(null);
                if (code != null && code.isValid()) {
                    code.setCurrentUses(code.getCurrentUses() + 1);
                    promoCodeRepository.save(code);
                }
            }
        }

        // Send confirmation email for pay-at-property bookings only.
        // For ONLINE_CARD payments the email is sent by sendReceiptEmail()
        // which the frontend calls after PayHere confirms the payment.
        if (saved.getPaymentMethod() != Booking.PaymentMethod.ONLINE_CARD) {
            try {
                String propertyName = saved.getProperty().getName();
                String roomName = saved.getRoomType().getRoomCategory().name();
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
            
            // Send Notification
            userRepository.findByEmailAndDeletedFalse(saved.getGuestEmail())
                .ifPresent(u -> notificationService.createNotification(u, 
                    "Booking Confirmed", 
                    "Your booking " + saved.getConfirmationCode() + " at " + saved.getProperty().getName() + " is confirmed." +
                    (saved.getNicNumber() != null && !saved.getNicNumber().isBlank() ? " Your secret passkey is: " + saved.getNicNumber() + ". Please present it at the property." : "")
                ));

        } else {
            log.info("[BOOKING] ONLINE_CARD booking {} created with PENDING status. Email will be sent after PayHere confirms payment.", saved.getConfirmationCode());
        }

        BookingResponse response = mapToResponse(saved);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingSseService.sendPropertyEvent(request.getPropertyId(), "new-booking", response);
            }
        });

        return response;
    }

    // ──────────────────────────────────────────
    // Send Confirmation Email (called by frontend
    // after PayHere redirect, since notify_url
    // cannot reach localhost in development)
    // ──────────────────────────────────────────
    @Transactional
    public void sendReceiptEmail(String confirmationCode) {
        Booking booking = bookingRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new com.b4code.backend.exceptions.ResourceNotFoundException(
                        "Booking not found: " + confirmationCode));

        if (booking.getPaymentMethod() != Booking.PaymentMethod.ONLINE_CARD) {
            log.info("[EMAIL] Skipping receipt email – booking {} is pay-at-property", confirmationCode);
            return;
        }

        if (booking.getStatus() == Booking.BookingStatus.PENDING || booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setIsPaid(true);
            bookingRepository.save(booking);

            // Update linked payment status & record transaction in Admin Finance Ledger
            paymentRepository.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(booking.getId(), com.b4code.backend.models.Payment.PaymentStatus.PENDING)
                .ifPresentOrElse(p -> {
                    p.setStatus(com.b4code.backend.models.Payment.PaymentStatus.SUCCESS);
                    paymentRepository.save(p);
                    paymentService.recordTransaction(p);
                }, () -> {
                    // Fallback: search for any existing payment for this booking
                    paymentRepository.findAll().stream()
                        .filter(p -> p.getBooking() != null && booking.getId().equals(p.getBooking().getId()))
                        .findFirst()
                        .ifPresent(p -> {
                            p.setStatus(com.b4code.backend.models.Payment.PaymentStatus.SUCCESS);
                            paymentRepository.save(p);
                            paymentService.recordTransaction(p);
                        });
                });

            log.info("[BOOKING] Confirmed booking {} after payment redirect and recorded transaction", confirmationCode);
        }

        String propertyName = booking.getRoomType().getProperty().getName();
        String roomName     = booking.getRoomType().getRoomCategory().name();

        log.info("[EMAIL] Sending receipt email to {} for booking {}", booking.getGuestEmail(), confirmationCode);
        emailService.sendBookingConfirmationEmail(
                booking.getGuestEmail(),
                booking.getGuestName(),
                booking.getConfirmationCode(),
                propertyName + " – " + roomName,
                booking.getCheckIn().toString(),
                booking.getCheckOut().toString(),
                booking.getTotalAmount().toString());
                
        // Send Notification
        userRepository.findByEmailAndDeletedFalse(booking.getGuestEmail())
            .ifPresent(u -> notificationService.createNotification(u, 
                "Booking Confirmed", 
                "Your booking " + booking.getConfirmationCode() + " at " + booking.getProperty().getName() + " is confirmed."
            ));
            
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingSseService.sendPropertyEvent(booking.getProperty().getId(), "booking-update", mapToResponse(booking));
            }
        });
    }

    // ──────────────────────────────────────────
    // Get by Confirmation Number
    // ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public BookingResponse getByConfirmationNumber(String confirmationNumber) {
        Booking booking = bookingRepository.findByConfirmationCode(confirmationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + confirmationNumber));
        return mapToResponse(booking);
    }

    // ──────────────────────────────────────────
    // Get all bookings for a guest (by email)
    // ──────────────────────────────────────────
    @Transactional(readOnly = true)
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

        BookingStatus oldStatus = booking.getStatus();

        if (oldStatus == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (oldStatus == BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Cannot cancel a booking that has already been checked in");
        }

        if (oldStatus == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a booking that has already been completed");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());
        Booking saved = bookingRepository.save(booking);

        // Free up daily inventory
        updateInventory(booking.getRoomType(), booking.getCheckIn(), booking.getCheckOut(), -booking.getRoomQuantity());

        // Calculate refund amount
        Property property = booking.getProperty();
        BigDecimal totalPaid = (oldStatus != BookingStatus.PENDING && booking.getPaymentMethod() == Booking.PaymentMethod.ONLINE_CARD)
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
            dispute.setDisputeId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
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

            // Send Notification for Refund Request
            userRepository.findByEmailAndDeletedFalse(saved.getGuestEmail())
                .ifPresent(u -> notificationService.createNotification(u, 
                    "Refund Request Sent", 
                    "A refund request for " + refundAmount + " has been sent to the property admin due to cancellation of " + saved.getConfirmationCode()
                ));
        }

        // We skip automatic payment gateway refund (paymentService.refundPayment) 
        // to let admin handle it via Dispute dashboard instead.
        
        // Send Notification for Cancellation
        userRepository.findByEmailAndDeletedFalse(saved.getGuestEmail())
            .ifPresent(u -> notificationService.createNotification(u, 
                "Booking Cancelled", 
                "Your booking " + saved.getConfirmationCode() + " has been successfully cancelled."
            ));

        BookingResponse response = mapToResponse(saved);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingSseService.sendPropertyEvent(booking.getProperty().getId(), "booking-update", response);
            }
        });

        return response;
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

        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Cannot modify a booking that has already been checked in");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot modify a booking that has already been completed");
        }

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + request.getRoomId()));

        if (!roomType.getProperty().getId().equals(request.getPropertyId())) {
            throw new IllegalArgumentException("Room does not belong to the selected property");
        }

        // Free up old inventory temporarily to check new availability
        updateInventory(booking.getRoomType(), booking.getCheckIn(), booking.getCheckOut(), -booking.getRoomQuantity());

        List<com.b4code.backend.models.RoomDateInventory> dailyInventories = roomDateInventoryRepository.findByRoomTypeIdAndDates(
                roomType.getId(), request.getCheckInDate(), request.getCheckOutDate());
        
        int peakBooked = dailyInventories.stream()
                .mapToInt(com.b4code.backend.models.RoomDateInventory::getBookedQuantity)
                .max().orElse(0);

        if (roomType.getInventory() - peakBooked < booking.getRoomQuantity()) {
            // Revert the temporary inventory freeing
            updateInventory(booking.getRoomType(), booking.getCheckIn(), booking.getCheckOut(), booking.getRoomQuantity());
            throw new RoomNotAvailableException("Room is not available for the selected dates with the current quantity");
        }

        String oldRoomCategory = booking.getRoomType().getRoomCategory().name();
        String oldDates = booking.getCheckIn().toString() + " to " + booking.getCheckOut().toString();

        List<String> currentPromoCodes = booking.getPromoCode() != null ? Arrays.asList(booking.getPromoCode().split(",")) : null;
        PriceBreakdown newPrice = calculatePrice(roomType, request.getCheckInDate(), request.getCheckOutDate(),
                booking.getRoomQuantity(), currentPromoCodes, false);
        BigDecimal previousTotal = booking.getTotalAmount();
        BigDecimal newTotal = newPrice.getTotalAmount();
        BigDecimal difference = newTotal.subtract(previousTotal);

        booking.setRoomType(roomType);
        booking.setCheckIn(request.getCheckInDate());
        booking.setCheckOut(request.getCheckOutDate());
        booking.setGuestCount(request.getGuests());
        booking.setTotalAmount(newTotal);
        booking.setTaxAmount(newPrice.getTaxAmount());
        booking.setDiscountAmount(newPrice.getDiscountAmount());
        booking.setPaymentMethod(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : booking.getPaymentMethod());

        Booking saved = bookingRepository.save(booking);

        // Consume new inventory
        updateInventory(roomType, request.getCheckInDate(), request.getCheckOutDate(), booking.getRoomQuantity());

        boolean isPaidOnline = saved.getPaymentMethod() == Booking.PaymentMethod.ONLINE_CARD;

        if (difference.compareTo(BigDecimal.ZERO) < 0 && isPaidOnline) {
            com.b4code.backend.models.Dispute dispute = new com.b4code.backend.models.Dispute();
            dispute.setDisputeId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            dispute.setBooking(saved);
            dispute.setProperty(saved.getProperty());
            
            // Link guest user if exists
            userRepository.findByEmailAndDeletedFalse(saved.getGuestEmail())
                .ifPresent(dispute::setGuest);

            dispute.setAmount(difference.abs());
            dispute.setReason("Booking modification refund request for " + saved.getConfirmationCode());
            dispute.setStatus(com.b4code.backend.models.enums.DisputeStatus.OPEN);
            
            disputeRepository.save(dispute);
            
            // Notify Admin
            adminNotificationService.createNotification(
                "Refund Request",
                "A refund request for " + difference.abs() + " has been created for booking " + saved.getConfirmationCode() + ".",
                com.b4code.backend.models.enums.AdminNotificationType.DISPUTE,
                dispute.getDisputeId()
            );
        }

        String newDates = saved.getCheckIn().toString() + " to " + saved.getCheckOut().toString();
        
        // Send booking modification email to guest
        try {
            String propertyName = saved.getProperty().getName();
            String newRoomCategory = saved.getRoomType().getRoomCategory().name();
            String diffStr = difference.compareTo(BigDecimal.ZERO) >= 0 ? difference.toString() : "-" + difference.abs().toString();
            emailService.sendBookingModificationEmail(
                    saved.getGuestEmail(),
                    saved.getGuestName(),
                    saved.getConfirmationCode(),
                    propertyName,
                    oldRoomCategory,
                    newRoomCategory,
                    oldDates,
                    newDates,
                    diffStr,
                    saved.getTotalAmount().toString()
            );
        } catch (Exception e) {
            log.error("Failed to send booking modification email for booking {}", saved.getConfirmationCode(), e);
        }
        
        // Send Notification for Modification
        userRepository.findByEmailAndDeletedFalse(saved.getGuestEmail())
            .ifPresent(u -> {
                notificationService.createNotification(u, 
                    "Booking Modified", 
                    "Your booking " + saved.getConfirmationCode() + " has been modified. New dates: " + newDates + "."
                );
                if (difference.compareTo(BigDecimal.ZERO) < 0 && isPaidOnline) {
                    notificationService.createNotification(u, 
                        "Refund Request Sent", 
                        "A refund request for " + difference.abs() + " has been sent due to your booking modification."
                    );
                }
            });

        BookingResponse response = mapToResponse(saved);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingSseService.sendPropertyEvent(saved.getProperty().getId(), "booking-update", response);
            }
        });

        return ModifyBookingResponse.builder()
                .booking(response)
                .previousTotalAmount(previousTotal)
                .newTotalAmount(newTotal)
                .refundAmount((isPaidOnline && difference.compareTo(BigDecimal.ZERO) < 0) ? difference.abs() : BigDecimal.ZERO)
                .additionalAmountDue((difference.compareTo(BigDecimal.ZERO) > 0) ? difference : BigDecimal.ZERO)
                .build();
    }

    // ──────────────────────────────────────────
    // Create Complaint
    // ──────────────────────────────────────────
    @Transactional
    public com.b4code.backend.dto.DisputeDto createComplaint(com.b4code.backend.dto.ComplaintRequestDto request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));

        com.b4code.backend.models.Dispute dispute = new com.b4code.backend.models.Dispute();
        dispute.setDisputeId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        dispute.setBooking(booking);
        dispute.setProperty(booking.getProperty());
        
        userRepository.findByEmailAndDeletedFalse(booking.getGuestEmail())
            .ifPresent(dispute::setGuest);

        dispute.setReason(request.getDescription());
        dispute.setCategory(request.getCategory());
        dispute.setSeverity(request.getSeverity());
        dispute.setRelatedOrderRef(request.getRelatedOrderRef());
        
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            dispute.setPhotoUrls(String.join(",", request.getPhotoUrls()));
        }

        dispute.setAmount(BigDecimal.ZERO);
        dispute.setStatus(com.b4code.backend.models.enums.DisputeStatus.OPEN);
        
        com.b4code.backend.models.Dispute saved = disputeRepository.save(dispute);
        
        // Send Notification
        userRepository.findByEmailAndDeletedFalse(booking.getGuestEmail())
            .ifPresent(u -> notificationService.createNotification(u, 
                "Complaint Submitted", 
                "Your complaint for booking " + booking.getConfirmationCode() + " has been received and is being reviewed."
            ));

        // Notify Admin
        adminNotificationService.createNotification(
            "New Guest Complaint",
            "A guest has submitted a complaint for booking " + booking.getConfirmationCode() + ". Please review it.",
            com.b4code.backend.models.enums.AdminNotificationType.DISPUTE,
            saved.getDisputeId()
        );

        return com.b4code.backend.dto.DisputeDto.fromEntity(saved);
    }

    // ──────────────────────────────────────────
    // Create Review
    // ──────────────────────────────────────────
    @Transactional
    public com.b4code.backend.dto.ReviewDTO.ReviewResponse createReview(com.b4code.backend.dto.ReviewDTO.CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));

        com.b4code.backend.models.Review review = new com.b4code.backend.models.Review();
        review.setBooking(booking);
        review.setProperty(booking.getProperty());
        
        userRepository.findByEmailAndDeletedFalse(booking.getGuestEmail())
            .ifPresent(review::setGuest);

        review.setOverallRating(request.getOverallRating());
        review.setCleanlinessRating(request.getCleanlinessRating());
        review.setComfortRating(request.getComfortRating());
        review.setServiceRating(request.getServiceRating());
        review.setDiningRating(request.getDiningRating());
        review.setLocationRating(request.getLocationRating());
        review.setValueRating(request.getValueRating());
        review.setComment(request.getComment());
        
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            review.setPhotoUrls(String.join(",", request.getPhotoUrls()));
        }

        com.b4code.backend.models.Review saved = reviewRepository.save(review);
        
        // Send Notification
        userRepository.findByEmailAndDeletedFalse(booking.getGuestEmail())
            .ifPresent(u -> notificationService.createNotification(u, 
                "Review Submitted", 
                "Your review for booking " + booking.getConfirmationCode() + " has been successfully submitted."
            ));

        return com.b4code.backend.dto.ReviewDTO.ReviewResponse.builder()
                .id(saved.getId())
                .bookingId(saved.getBooking().getId())
                .propertyId(saved.getProperty().getId())
                .guestId(saved.getGuest() != null ? saved.getGuest().getId() : null)
                .overallRating(saved.getOverallRating())
                .cleanlinessRating(saved.getCleanlinessRating())
                .comfortRating(saved.getComfortRating())
                .serviceRating(saved.getServiceRating())
                .diningRating(saved.getDiningRating())
                .locationRating(saved.getLocationRating())
                .valueRating(saved.getValueRating())
                .comment(saved.getComment())
                .photoUrls(saved.getPhotoUrls() != null ? Arrays.asList(saved.getPhotoUrls().split(",")) : null)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // ──────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────

    private PriceBreakdown calculatePrice(RoomType roomType, LocalDate checkIn, LocalDate checkOut, Integer roomQuantity, List<String> promoCodes,
            boolean isPreview) {
        BigDecimal pricePerNight = roomType.getPricePerNight();

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
                
                if (code.getPropertyId() != null && !code.getPropertyId().equals(roomType.getProperty().getId())) {
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
                .roomId(roomType.getId())
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

        BookingResponse response = BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoomType().getId())
                .roomName(booking.getRoomType().getRoomCategory().name())
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
                .passkey(booking.getNicNumber())
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

    private RoomType findRoomOrThrow(Long id) {
        return roomTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }

    private void updateInventory(RoomType roomType, LocalDate checkIn, LocalDate checkOut, int deltaQuantity) {
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            com.b4code.backend.models.RoomDateInventory inventory = roomDateInventoryRepository.findByRoomTypeIdAndDate(roomType.getId(), date)
                    .orElse(com.b4code.backend.models.RoomDateInventory.builder()
                            .roomType(roomType)
                            .date(date)
                            .bookedQuantity(0)
                            .build());
            
            inventory.setBookedQuantity(inventory.getBookedQuantity() + deltaQuantity);
            roomDateInventoryRepository.save(inventory);
        }
    }
}

