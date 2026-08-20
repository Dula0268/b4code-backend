package com.b4code.backend.service.impl;

import com.b4code.backend.dao.AvailabilityRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.AvailabilityBulkUpdateRequest;
import com.b4code.backend.dto.owner.AvailabilityDayDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Availability;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerAvailabilityServiceImpl implements OwnerAvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityDayDto> getWeeklyCalendar(String ownerEmail, Long propertyId, String baseDateStr) {
        verifyOwnsProperty(ownerEmail, propertyId);
        LocalDate base = baseDateStr != null ? LocalDate.parse(baseDateStr) : LocalDate.now();
        LocalDate from = base;
        LocalDate to = base.plusDays(6);
        return buildDtos(propertyId, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityDayDto> getMonthlyCalendar(String ownerEmail, Long propertyId, int year, int month) {
        verifyOwnsProperty(ownerEmail, propertyId);
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        return buildDtos(propertyId, from, to);
    }

    @Override
    @Transactional
    public void bulkUpdate(String ownerEmail, AvailabilityBulkUpdateRequest request) {
        verifyOwnsProperty(ownerEmail, request.getPropertyId());
        List<Room> rooms = roomRepository.findByPropertyId(request.getPropertyId());

        for (String dateStr : request.getDates()) {
            LocalDate date = LocalDate.parse(dateStr);
            for (Room room : rooms) {
                Availability avail = availabilityRepository
                        .findByRoomIdAndDate(room.getId(), date)
                        .orElse(Availability.builder().room(room).date(date).build());
                avail.setStatus(request.getNewStatus() != null ? request.getNewStatus() : "AVAILABLE");
                avail.setCustomPrice(request.getCustomPrice());
                avail.setNotes(request.getNotes());
                availabilityRepository.save(avail);
            }
        }
    }

    private List<AvailabilityDayDto> buildDtos(Long propertyId, LocalDate from, LocalDate to) {
        List<Availability> records = availabilityRepository.findByPropertyAndDateRange(propertyId, from, to);
        Map<String, Availability> index = records.stream()
                .collect(Collectors.toMap(
                        a -> a.getRoom().getId() + "_" + a.getDate(),
                        a -> a,
                        (a, b) -> a));

        List<Room> rooms = roomRepository.findByPropertyId(propertyId);
        List<AvailabilityDayDto> result = new ArrayList<>();
        for (Room room : rooms) {
            LocalDate d = from;
            while (!d.isAfter(to)) {
                String key = room.getId() + "_" + d;
                Availability a = index.get(key);
                result.add(AvailabilityDayDto.builder()
                        .roomId(room.getId())
                        .roomName(room.getName())
                        .date(d.toString())
                        .status(a != null ? a.getStatus() : "AVAILABLE")
                        .customPrice(a != null && a.getCustomPrice() != null ? a.getCustomPrice().toPlainString() : null)
                        .notes(a != null ? a.getNotes() : null)
                        .availabilityId(a != null ? a.getId() : null)
                        .build());
                d = d.plusDays(1);
            }
        }
        return result;
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }

    private void verifyOwnsProperty(String ownerEmail, Long propertyId) {
        User owner = resolveOwner(ownerEmail);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
    }
}
