package com.b4code.backend.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import com.b4code.backend.models.enums.OrderStatus;

@Data
public class OrderRequest {
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    private Long guestId;
    
    @NotBlank(message = "Guest Session ID is required")
    private String guestSessionId;
    
    private String location;
    
    @NotBlank(message = "Guest Name is required")
    private String guestName;
    
    @NotBlank(message = "Guest Phone is required")
    private String guestPhone;
    
    @NotNull(message = "Total Amount is required")
    @Min(value = 0, message = "Total amount cannot be negative")
    private Double totalAmount;
    
    private OrderStatus status;
    private String guestInstructions;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    @Valid
    @NotNull(message = "Items list cannot be null")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price cannot be negative")
        private Double priceAtOrder;
        
        private String note;
    }
}

