package com.b4code.backend.service;

import com.b4code.backend.dto.owner.AvailabilityBulkUpdateRequest;
import com.b4code.backend.dto.owner.AvailabilityDayDto;

import java.util.List;

public interface OwnerAvailabilityService {
    List<AvailabilityDayDto> getWeeklyCalendar(String ownerEmail, Long propertyId, String baseDate);
    List<AvailabilityDayDto> getMonthlyCalendar(String ownerEmail, Long propertyId, int year, int month);
    void bulkUpdate(String ownerEmail, AvailabilityBulkUpdateRequest request);
}
