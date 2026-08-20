package com.b4code.backend.service;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.OwnerReservationDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Transaction;
import com.b4code.backend.models.enums.TransactionType;
import com.b4code.backend.models.User;
import com.b4code.backend.dao.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StaffReservationService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BookingSseService bookingSseService;

    @Transactional(readOnly = true)
    public List<OwnerReservationDto> listReservations(String staffEmail, Long propertyId, String search, String statusParam) {
        verifyStaffAccess(staffEmail, propertyId);

        Booking.BookingStatus tempStatus = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try { tempStatus = Booking.BookingStatus.valueOf(statusParam.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        final Booking.BookingStatus status = tempStatus;
        
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        
        // Use a simpler approach if the specific repository method doesn't exist
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findAll().stream()
                    .filter(b -> b.getProperty() != null && b.getProperty().getId().equals(propertyId) && b.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            bookings = bookingRepository.findAll().stream()
                    .filter(b -> b.getProperty() != null && b.getProperty().getId().equals(propertyId))
                    .collect(Collectors.toList());
        }

        if (searchTerm != null) {
            final String lowerSearch = searchTerm.toLowerCase();
            bookings = bookings.stream()
                    .filter(b -> (b.getConfirmationCode() != null && b.getConfirmationCode().toLowerCase().contains(lowerSearch)) ||
                                 (b.getGuestName() != null && b.getGuestName().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }
        
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .map(OwnerReservationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OwnerReservationDto checkIn(String staffEmail, Long propertyId, Long bookingId) {
        verifyStaffAccess(staffEmail, propertyId);
        Booking booking = getBookingForProperty(propertyId, bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new CustomException("Booking must be CONFIRMED to check in", HttpStatus.BAD_REQUEST);
        }
        
        if (booking.getPaymentMethod() == Booking.PaymentMethod.PAY_AT_PROPERTY && !Boolean.TRUE.equals(booking.getIsPaid())) {
            throw new CustomException("Payment must be collected before check-in", HttpStatus.BAD_REQUEST);
        }
        
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        Booking saved = bookingRepository.save(booking);
        OwnerReservationDto dto = OwnerReservationDto.fromEntity(saved);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingSseService.sendPropertyEvent(propertyId, "booking-update", dto);
            }
        });
        return dto;
    }

    @Transactional
    public OwnerReservationDto checkOut(String staffEmail, Long propertyId, Long bookingId) {
        verifyStaffAccess(staffEmail, propertyId);
        Booking booking = getBookingForProperty(propertyId, bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new CustomException("Booking must be CHECKED_IN to check out", HttpStatus.BAD_REQUEST);
        }
        
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        Booking saved = bookingRepository.save(booking);
        OwnerReservationDto dto = OwnerReservationDto.fromEntity(saved);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingSseService.sendPropertyEvent(propertyId, "booking-update", dto);
            }
        });
        return dto;
    }

    @Transactional
    public OwnerReservationDto takePayment(String staffEmail, Long propertyId, Long bookingId, String nicNumber) {
        verifyStaffAccess(staffEmail, propertyId);
        Booking booking = getBookingForProperty(propertyId, bookingId);
        
        if (Boolean.TRUE.equals(booking.getIsPaid())) {
            throw new CustomException("Booking is already paid", HttpStatus.BAD_REQUEST);
        }
        
        if (nicNumber != null && !nicNumber.isBlank()) {
            booking.setNicNumber(nicNumber.trim());
        }
        
        booking.setIsPaid(true);
        
        // Create a transaction to instantly reflect this in the revenue
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber("PAY-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setAmount(booking.getTotalAmount());
        transaction.setCurrency("LKR");
        transaction.setType(TransactionType.BOOKING_PAYMENT);
        transaction.setProperty(booking.getProperty());
        
        // Optionally attach the staff user who took the payment
        User staff = userRepository.findByEmail(staffEmail).orElse(null);
        transaction.setUser(staff);
        transaction.setDescription("Physical payment collected at property for booking " + booking.getConfirmationCode());
        
        transactionRepository.save(transaction);
        Booking saved = bookingRepository.save(booking);
        OwnerReservationDto dto = OwnerReservationDto.fromEntity(saved);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingSseService.sendPropertyEvent(propertyId, "booking-update", dto);
            }
        });
        return dto;
    }

    private void verifyStaffAccess(String staffEmail, Long propertyId) {
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (!propertyId.equals(staff.getPropertyId())) {
            throw new CustomException("Staff does not belong to this property", HttpStatus.FORBIDDEN);
        }
    }

    private Booking getBookingForProperty(Long propertyId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found", HttpStatus.NOT_FOUND));
                
        if (booking.getProperty() == null || !propertyId.equals(booking.getProperty().getId())) {
            throw new CustomException("Booking does not belong to this property", HttpStatus.FORBIDDEN);
        }
        
        return booking;
    }
}
