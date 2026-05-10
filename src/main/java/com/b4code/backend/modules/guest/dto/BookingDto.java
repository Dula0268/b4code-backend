package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.modules.guest.models.Booking.BookingStatus;
import com.b4code.backend.modules.guest.models.Booking.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

        @NotBlank(message = "Guest name is required")
        private String guestName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String guestEmail;

        @NotBlank(message = "Phone number is required")
        private String guestPhone;

        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in cannot be in the past")
        private LocalDate checkIn;

        @NotNull(message = "Check-out date is required")
        @Future(message = "Check-out must be in the future")
        private LocalDate checkOut;

        @NotNull(message = "Guest count is required")
        @Min(value = 1, message = "At least 1 guest required")
        private Integer guestCount;

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
        private String roomName;
        private Integer nights;
        private BigDecimal pricePerNight;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;       // 10% tax
        private BigDecimal totalAmount;
        private String promoApplied;
    }

    // ──────────────────────────────────
    // Booking Confirmation Response
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingResponse {
        private Long bookingId;
        private Long propertyId;
        private Long roomId;
        private String confirmationNumber;
        private String guestName;
        private String guestEmail;
        private String propertyName;
        private String propertyAddress;
        private String roomName;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer nights;
        private Integer guestCount;
        private BigDecimal totalAmount;
        private BookingStatus status;
        private PaymentMethod paymentMethod;
        private LocalDateTime createdAt;
    }

    // ──────────────────────────────────
    // Cancel / Modify Request
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelBookingRequest {
        @NotBlank(message = "Reason is required")
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyBookingRequest {
        private Long guestId;

        @NotNull(message = "Property ID is required")
        private Long propertyId;

        @NotNull(message = "Room ID is required")
        private Long roomId;

        @NotNull(message = "Check-in date is required")
        private LocalDate checkInDate;

        @NotNull(message = "Check-out date is required")
        private LocalDate checkOutDate;

        @NotNull(message = "Guest count is required")
        @Min(value = 1, message = "At least 1 guest required")
        private Integer guests;

        private String specialRequests;
        private PaymentMethod paymentMethod;
        private BigDecimal totalPrice;
    }

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