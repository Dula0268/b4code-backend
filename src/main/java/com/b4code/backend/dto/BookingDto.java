package com.b4code.backend.dto;

import com.b4code.backend.models.Booking.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDto {

    // ──────────────────────────────────
    // Create Booking Request
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateBookingRequest {

        @NotNull(message = "Room ID is required")
        private Long roomId;
        
        @NotNull(message = "Property ID is required")
        private Long propertyId;

        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in cannot be in the past")
        private LocalDate checkIn;

        @NotNull(message = "Check-out date is required")
        @Future(message = "Check-out must be in the future")
        private LocalDate checkOut;

        @NotNull(message = "Adults count is required")
        @Min(value = 1, message = "At least 1 adult required")
        private Integer adults;

        @NotNull(message = "Children count is required")
        @Min(value = 0, message = "Children count cannot be negative")
        private Integer children;

        private String promoCode;

        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;
    }

    // ──────────────────────────────────
    // Price Breakdown (Preview)
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceBreakdown {
        private Long roomId;
        private Integer nights;
        private BigDecimal pricePerNight;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private String promoApplied;
    }

    // ──────────────────────────────────
    // Booking Response
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingResponse {
        private Long id;
        private Long roomId;
        private Long propertyId;
        private Long reviewId;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer adults;
        private Integer children;
        private String promoCode;
        private PaymentMethod paymentMethod;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
    }

    // ──────────────────────────────────
    // Cancel Booking Request
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancelBookingRequest {
        private String reason;
    }

    // ──────────────────────────────────
    // Modify Booking Request
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModifyBookingRequest {
        @NotNull(message = "Room ID is required")
        private Long roomId;

        @NotNull(message = "Property ID is required")
        private Long propertyId;

        @NotNull(message = "Check-in date is required")
        private LocalDate checkInDate;

        @NotNull(message = "Check-out date is required")
        private LocalDate checkOutDate;

        @NotNull(message = "Guest count is required")
        @Min(value = 1, message = "At least 1 guest required")
        private Integer guests;

        private PaymentMethod paymentMethod;
    }

    // ──────────────────────────────────
    // Modify Booking Response
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModifyBookingResponse {
        private BookingResponse booking;
        private BigDecimal previousTotalAmount;
        private BigDecimal newTotalAmount;
        private BigDecimal refundAmount;
        private BigDecimal additionalAmountDue;
    }
}
