package com.b4code.backend.service.impl;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomTypeRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.OwnerDashboardDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerDashboardServiceImpl implements OwnerDashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public OwnerDashboardDto getDashboard(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
        Long ownerId = owner.getId();

        long totalProperties = propertyRepository.findByOwnerId(ownerId).size();
        long totalRoomTypes = roomTypeRepository.countByOwner(ownerId);
        long activeBookings = bookingRepository.countActiveByOwner(ownerId);
        BigDecimal revenue = bookingRepository.sumRevenueByOwner(ownerId);
        if (revenue == null) revenue = BigDecimal.ZERO;

        List<Booking> recent = bookingRepository.findRecentByOwner(ownerId, PageRequest.of(0, 5));
        List<OwnerDashboardDto.RecentBookingDto> recentDtos = recent.stream()
                .map(b -> OwnerDashboardDto.RecentBookingDto.builder()
                        .id(b.getId())
                        .confirmationCode(b.getConfirmationCode())
                        .guestName(b.getGuestName())
                        .propertyName(b.getProperty() != null ? b.getProperty().getName() : null)
                        .checkIn(b.getCheckIn() != null ? b.getCheckIn().toString() : null)
                        .checkOut(b.getCheckOut() != null ? b.getCheckOut().toString() : null)
                        .status(b.getStatus() != null ? b.getStatus().name() : null)
                        .totalAmount(b.getTotalAmount() != null ? b.getTotalAmount().toPlainString() : "0")
                        .build())
                .toList();

        return OwnerDashboardDto.builder()
                .totalBookings(bookingRepository.countActiveByOwner(ownerId))
                .activeBookings(activeBookings)
                .totalRevenue(revenue.toPlainString())
                .totalProperties(totalProperties)
                .totalRoomTypes(totalRoomTypes)
                .recentBookings(recentDtos)
                .build();
    }
}
