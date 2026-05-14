package com.b4code.backend.modules.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // ── Metric Cards ──
    private MetricCard totalProperties;
    private MetricCard totalRooms;
    private MetricCard activeReservations;
    private MetricCard todayCheckIns;
    private MetricCard todayCheckOuts;
    private MetricCard totalRevenue;

    // ── Calendar / Availability ──
    private AvailabilityPreview availabilityPreview;

    // ── Recent Reservations ──
    private List<ReservationDto> recentReservations;

    // ── Alert Cards ──
    private AlertCard newReviews;
    private AlertCard maintenanceAlerts;
    private AlertCard growth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricCard {
        private String label;
        private String value;
        private String change;       // e.g. "+2%", "-5%", "High"
        private boolean highlight;   // true for accent-colored card (e.g. revenue)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityPreview {
        private int year;
        private int month;              // 0-indexed (0=Jan, 11=Dec)
        private String monthName;
        private double occupancyPercent; // e.g. 85.0
        private List<Integer> peakDays;  // days with high bookings
        private List<KeyHighlight> keyHighlights;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyHighlight {
        private String color;   // hex color e.g. "#e74c3c"
        private String message; // e.g. "Peak booking: Oct 2nd - 4th"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationDto {
        private Long id;
        private String initials;
        private String initialsColor;
        private String guestName;
        private String propertyName;
        private String roomName;
        private String dates;          // formatted e.g. "10 OCT - 14 OCT"
        private String nights;         // e.g. "4 Nights"
        private String status;         // CHECKED_IN, CONFIRMED, PENDING, CHECKED_OUT
        private String statusColor;
        private String statusBg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertCard {
        private String label;   // e.g. "NEW REVIEWS"
        private String value;   // e.g. "12 Unread"
        private String icon;    // icon identifier for frontend
        private String iconColor;
        private String iconBg;
    }
}
