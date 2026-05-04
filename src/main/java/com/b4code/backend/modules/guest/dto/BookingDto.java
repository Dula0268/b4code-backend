package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.modules.guest.entity.Booking;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {

    private Long id;
    private Long propertyId;
    private Long guestId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private Double totalPrice;
    private String createdAt;

    public static BookingDto fromEntity(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .propertyId(booking.getPropertyId())
                .guestId(booking.getGuestId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : null)
                .build();
    }
}
