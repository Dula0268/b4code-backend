package com.b4code.backend.modules.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    @NotNull(message = "Guest ID is required")
    private Long guestId;
    
    private String roomNumber;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be greater than 0")
    private Double totalAmount;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private LocalDateTime createdAt;
}
