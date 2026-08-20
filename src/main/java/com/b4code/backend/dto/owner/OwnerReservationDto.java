package com.b4code.backend.dto.owner;

import com.b4code.backend.models.Booking;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerReservationDto {
    private Long id;
    private String confirmationCode;
    private String guestName;
    private String guestEmail;
    private String nicNumber;
    private String checkIn;
    private String checkOut;
    private Integer adults;
    private Integer children;
    private Long roomId;
    private String roomName;
    private String roomNumber;
    private Long propertyId;
    private String propertyName;
    private String status;
    private String paymentMethod;
    private Boolean isPaid;
    private String paymentMethodStr;
    private String totalAmount;
    private String taxAmount;
    private String discountAmount;
    private String createdAt;
    private Boolean isManual;

    public static OwnerReservationDto fromEntity(Booking b) {
        String roomName = b.getRoomType() != null ? b.getRoomType().getName() : null;
        Long roomId = b.getRoomType() != null ? b.getRoomType().getId() : null;
        Long propId = b.getProperty() != null ? b.getProperty().getId() : null;
        String propName = b.getProperty() != null ? b.getProperty().getName() : null;

        return OwnerReservationDto.builder()
                .id(b.getId())
                .confirmationCode(b.getConfirmationCode())
                .guestName(b.getGuestName())
                .guestEmail(b.getGuestEmail())
                .nicNumber(b.getNicNumber())
                .checkIn(b.getCheckIn() != null ? b.getCheckIn().toString() : null)
                .checkOut(b.getCheckOut() != null ? b.getCheckOut().toString() : null)
                .adults(b.getAdults())
                .children(b.getChildren())
                .roomId(roomId)
                .roomName(roomName)
                .roomNumber(b.getRoomNumber())
                .propertyId(propId)
                .propertyName(propName)
                .status(b.getStatus() != null ? b.getStatus().name() : null)
                .paymentMethod(b.getPaymentMethod() != null ? b.getPaymentMethod().name() : null)
                .isPaid(b.getIsPaid())
                .totalAmount(b.getTotalAmount() != null ? b.getTotalAmount().toPlainString() : "0")
                .taxAmount(b.getTaxAmount() != null ? b.getTaxAmount().toPlainString() : "0")
                .discountAmount(b.getDiscountAmount() != null ? b.getDiscountAmount().toPlainString() : "0")
                .createdAt(b.getCreatedAt() != null ? b.getCreatedAt().toString() : null)
                .isManual(false)
                .build();
    }
}
