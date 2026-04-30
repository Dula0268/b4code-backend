package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.AvailabilityDto.*;
import com.b4code.backend.modules.owner.entity.RoomAvailability;
import com.b4code.backend.modules.owner.repository.OwnerPropertyRepository;
import com.b4code.backend.modules.owner.repository.RoomAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final RoomAvailabilityRepository availabilityRepository;
    private final OwnerPropertyRepository propertyRepository;

    public WeeklyCalendarResponse getWeeklyCalendar(Long propertyId, LocalDate baseDate) {
        LocalDate sun = baseDate.with(DayOfWeek.SUNDAY).minusWeeks(1).plusDays(baseDate.getDayOfWeek() == DayOfWeek.SUNDAY ? 7 : 0);
        if (baseDate.getDayOfWeek() != DayOfWeek.SUNDAY) sun = baseDate.minusDays(baseDate.getDayOfWeek().getValue());
        LocalDate sat = sun.plusDays(6);

        List<RoomAvailability> entries = availabilityRepository.findByPropertyIdAndDateBetweenOrderByDateAsc(propertyId, sun, sat);
        Map<LocalDate, RoomAvailability> map = entries.stream().collect(Collectors.toMap(RoomAvailability::getDate, e -> e, (a, b) -> a));

        List<DayCell> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = sun.plusDays(i);
            DayCell cell = new DayCell();
            cell.setDate(d);
            RoomAvailability entry = map.get(d);
            if (entry != null) { cell.setStatus(entry.getStatus()); cell.setGuestName(entry.getGuestName()); cell.setPrice(entry.getCustomPrice()); }
            else { cell.setStatus("AVAILABLE"); }
            days.add(cell);
        }

        WeeklyCalendarResponse resp = new WeeklyCalendarResponse();
        resp.setMonthYear(sun.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + sun.getYear());
        resp.setDays(days);
        resp.setPropertyId(propertyId);
        return resp;
    }

    public MonthlyCalendarResponse getMonthlyCalendar(Long propertyId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<RoomAvailability> entries = availabilityRepository.findByPropertyIdAndDateBetweenOrderByDateAsc(propertyId, start, end);
        Map<LocalDate, RoomAvailability> map = entries.stream().collect(Collectors.toMap(RoomAvailability::getDate, e -> e, (a, b) -> a));

        List<DayCell> days = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayCell cell = new DayCell();
            cell.setDate(d);
            RoomAvailability entry = map.get(d);
            if (entry != null) { cell.setStatus(entry.getStatus()); cell.setGuestName(entry.getGuestName()); cell.setPrice(entry.getCustomPrice()); }
            else { cell.setStatus("AVAILABLE"); }
            days.add(cell);
        }

        MonthlyCalendarResponse resp = new MonthlyCalendarResponse();
        resp.setMonthYear(start.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year);
        resp.setYear(year); resp.setMonth(month); resp.setDays(days); resp.setPropertyId(propertyId);
        return resp;
    }

    @Transactional
    public void updateAvailability(AvailabilityUpdateRequest request) {
        for (LocalDate date : request.getDates()) {
            RoomAvailability entry = availabilityRepository.findByRoomIdAndDate(request.getRoomId(), date);
            if (entry == null) {
                entry = new RoomAvailability();
                entry.setRoomId(request.getRoomId());
                entry.setPropertyId(request.getPropertyId());
                entry.setDate(date);
            }
            entry.setStatus(request.getNewStatus());
            entry.setCustomPrice(request.getCustomPrice());
            entry.setNotes(request.getNotes());
            availabilityRepository.save(entry);
        }
        log.info("Availability updated for room {} on {} dates", request.getRoomId(), request.getDates().size());
    }
}
