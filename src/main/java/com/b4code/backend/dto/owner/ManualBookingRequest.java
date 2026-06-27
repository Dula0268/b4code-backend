package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ManualBookingRequest {
    private Long propertyId;
    private Long roomId;
    private String guestName;
    private String guestEmail;
    private String nicNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer adults;
    private Integer children;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String notes;
}
