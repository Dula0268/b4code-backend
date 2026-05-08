package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Reservation module
 */
public class ReservationDto {

    @Data
    public static class ManualBookingRequest {
        private Long propertyId;
        private Long roomId;
        private String guestName;
        private String guestEmail;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private BigDecimal totalPrice;
        private String paymentStatus;
        private String notes;
    }

    @Data
    public static class ReservationResponse {
        private Long id;
        private String guestName;
        private String guestEmail;
        private String guestTier;
        private String guestInitials;
        private String propertyName;
        private String roomName;
        private String checkIn;
        private String checkOut;
        private String paymentStatus;
        private String status;
        private BigDecimal totalPrice;
    }

    @Data
    public static class ReservationKpiResponse {
        private int confirmed;
        private int pending;
        private int checkInsToday;
        private int cancellations;
        private int totalBookingsThisMonth;
        private List<ReservationResponse> reservations;
        private int currentPage;
        private int totalPages;
        private long totalItems;
    }
}
