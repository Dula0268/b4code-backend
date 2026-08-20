package com.b4code.backend.service.impl;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomTypeRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.ManualBookingRequest;
import com.b4code.backend.dto.owner.OwnerReservationDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.RoomType;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerReservationServiceImpl implements OwnerReservationService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OwnerReservationDto> listReservations(String ownerEmail, String search, String statusParam) {
        User owner = resolveOwner(ownerEmail);
        Booking.BookingStatus status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try { status = Booking.BookingStatus.valueOf(statusParam.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        return bookingRepository.findByOwnerWithFilters(owner.getId(), status, searchTerm)
                .stream().map(OwnerReservationDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerReservationDto getReservation(String ownerEmail, Long id) {
        return OwnerReservationDto.fromEntity(resolveOwnedBooking(ownerEmail, id));
    }

    @Override
    @Transactional
    public OwnerReservationDto createManualBooking(String ownerEmail, ManualBookingRequest request) {
        User owner = resolveOwner(ownerEmail);

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Property does not belong to this owner", HttpStatus.FORBIDDEN);
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomId())
                .orElseThrow(() -> new CustomException("Room not found", HttpStatus.NOT_FOUND));

        BigDecimal total = request.getTotalAmount() != null ? request.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal tax = total.multiply(new BigDecimal("0.10"));

        Booking.PaymentMethod payMethod = Booking.PaymentMethod.PAY_AT_PROPERTY;
        if (request.getPaymentMethod() != null) {
            try { payMethod = Booking.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        Booking booking = Booking.builder()
                .confirmationCode("MAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail() != null ? request.getGuestEmail() : "manual@booking.local")
                .nicNumber(request.getNicNumber())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .adults(request.getAdults() != null ? request.getAdults() : 1)
                .children(request.getChildren() != null ? request.getChildren() : 0)
                .roomType(roomType)
                .property(property)
                .paymentMethod(payMethod)
                .status(Booking.BookingStatus.CONFIRMED)
                .totalAmount(total)
                .taxAmount(tax)
                .discountAmount(BigDecimal.ZERO)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Owner {} created manual booking id={}", ownerEmail, saved.getId());
        return OwnerReservationDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerReservationDto checkIn(String ownerEmail, Long id) {
        Booking booking = resolveOwnedBooking(ownerEmail, id);
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new CustomException("Booking must be CONFIRMED to check in", HttpStatus.BAD_REQUEST);
        }
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        return OwnerReservationDto.fromEntity(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public OwnerReservationDto checkOut(String ownerEmail, Long id) {
        Booking booking = resolveOwnedBooking(ownerEmail, id);
        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new CustomException("Booking must be CHECKED_IN to check out", HttpStatus.BAD_REQUEST);
        }
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        return OwnerReservationDto.fromEntity(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public OwnerReservationDto cancel(String ownerEmail, Long id) {
        Booking booking = resolveOwnedBooking(ownerEmail, id);
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED ||
            booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new CustomException("Cannot cancel a completed or already cancelled booking", HttpStatus.BAD_REQUEST);
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return OwnerReservationDto.fromEntity(bookingRepository.save(booking));
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }

    private Booking resolveOwnedBooking(String ownerEmail, Long bookingId) {
        User owner = resolveOwner(ownerEmail);
        return bookingRepository.findByIdAndPropertyOwnerId(bookingId, owner.getId())
                .orElseThrow(() -> new CustomException(
                        "Booking not found or does not belong to this owner", HttpStatus.NOT_FOUND));
    }
}
