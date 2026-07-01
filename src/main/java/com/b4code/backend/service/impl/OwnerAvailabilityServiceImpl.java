package com.b4code.backend.service.impl;

import com.b4code.backend.dao.AvailabilityRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomBookingDailyRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.AvailabilityBulkUpdateRequest;
import com.b4code.backend.dto.owner.AvailabilityDayDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Availability;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.RoomBookingDaily;
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
    private final RoomBookingDailyRepository roomBookingDailyRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityDayDto> getWeeklyCalendar(String ownerEmail, Long propertyId, String baseDateStr) {
        verifyPropertyExists(propertyId);
        LocalDate base = baseDateStr != null ? LocalDate.parse(baseDateStr) : LocalDate.now();
        return buildDtos(propertyId, base, base.plusDays(6));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityDayDto> getMonthlyCalendar(String ownerEmail, Long propertyId, int year, int month) {
        verifyPropertyExists(propertyId);
        YearMonth ym = YearMonth.of(year, month);
        return buildDtos(propertyId, ym.atDay(1), ym.atEndOfMonth());
    }

    @Override
    @Transactional
    public void bulkUpdate(String ownerEmail, AvailabilityBulkUpdateRequest request) {
        verifyPropertyExists(request.getPropertyId());
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
        // availability overrides (custom status / price / notes)
        List<Availability> records = availabilityRepository.findByPropertyAndDateRange(propertyId, from, to);
        Map<String, Availability> availIndex = records.stream()
                .collect(Collectors.toMap(
                        a -> a.getRoom().getId() + "_" + a.getDate(),
                        a -> a,
                        (a, b) -> a));

        // booking density from room_booking_daily
        List<RoomBookingDaily> dailyRecords = roomBookingDailyRepository.findByPropertyAndDateRange(propertyId, from, to);
        Map<String, Integer> bookingIndex = dailyRecords.stream()
                .collect(Collectors.toMap(
                        r -> r.getRoom().getId() + "_" + r.getDate(),
                        RoomBookingDaily::getBookedQuantity,
                        Integer::sum));

        List<Room> rooms = roomRepository.findByPropertyId(propertyId);
        List<AvailabilityDayDto> result = new ArrayList<>();

        for (Room room : rooms) {
            int totalInventory = room.getInventory() != null ? room.getInventory() : 0;
            LocalDate d = from;
            while (!d.isAfter(to)) {
                String key = room.getId() + "_" + d;
                Availability a = availIndex.get(key);
                int booked = bookingIndex.getOrDefault(key, 0);
                int available = Math.max(0, totalInventory - booked);

                result.add(AvailabilityDayDto.builder()
                        .roomId(room.getId())
                        .roomName(room.getName())
                        .date(d.toString())
                        .status(a != null ? a.getStatus() : "AVAILABLE")
                        .customPrice(a != null && a.getCustomPrice() != null ? a.getCustomPrice().toPlainString() : null)
                        .notes(a != null ? a.getNotes() : null)
                        .availabilityId(a != null ? a.getId() : null)
                        .inventory(totalInventory)
                        .bookedQuantity(booked)
                        .availableQuantity(available)
                        .build());
                d = d.plusDays(1);
            }
        }
        return result;
    }

    private void verifyPropertyExists(Long propertyId) {
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }
}
