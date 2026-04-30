package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs for Availability module — weekly and monthly calendar data
 */
public class AvailabilityDto {

    @Data
    public static class DayCell {
        private LocalDate date;
        private String status; // AVAILABLE, BOOKED, BLOCKED
        private String guestName;
        private BigDecimal price;
    }

    @Data
    public static class WeeklyCalendarResponse {
        private String monthYear;
        private List<DayCell> days; // 7 days
        private Long propertyId;
        private String propertyName;
        private double occupancyPercent;
        private BigDecimal revenueMtd;
    }

    @Data
    public static class MonthlyCalendarResponse {
        private String monthYear;
        private int year;
        private int month;
        private List<DayCell> days; // 28-31 days
        private Long propertyId;
        private String propertyName;
    }

    @Data
    public static class AvailabilityUpdateRequest {
        private Long propertyId;
        private Long roomId;
        private List<LocalDate> dates;
        private String newStatus; // AVAILABLE, BOOKED, BLOCKED
        private BigDecimal customPrice;
        private String notes;
    }

    @Data
    public static class BookingDetailResponse {
        private Long reservationId;
        private String guestName;
        private String guestEmail;
        private String propertyName;
        private String roomName;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private String status;
        private BigDecimal totalPrice;
    }
}
