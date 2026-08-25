package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OwnerDashboardDto {
    private long totalBookings;
    private long activeBookings;
    private String totalRevenue;
    private String monthRevenue;
    private long totalProperties;
    private long totalRoomTypes;
    private List<RecentBookingDto> recentBookings;

    @Data
    @Builder
    public static class RecentBookingDto {
        private Long id;
        private String confirmationCode;
        private String guestName;
        private String propertyName;
        private String checkIn;
        private String checkOut;
        private String status;
        private String totalAmount;
    }
}
