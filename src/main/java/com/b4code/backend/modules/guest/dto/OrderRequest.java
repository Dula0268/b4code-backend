package com.b4code.backend.modules.guest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Request object for placing a new guest order")
public class OrderRequest {
    @Schema(description = "ID of the property where order is placed", example = "1")
    private Long propertyId;
    
    @Schema(description = "ID of the guest placing the order", example = "101")
    private Long guestId;
    
    @Schema(description = "Room number for delivery", example = "302")
    private String roomNumber;
    
    @Schema(description = "Total cost of the order", example = "45.50")
    private Double totalAmount;
    
    @Schema(description = "Initial status (usually PENDING)", example = "PENDING")
    private String status;
    
    @Schema(description = "List of menu items in the order")
    private List<OrderItemRequest> items;

    @Data
    @Schema(description = "Individual item in the order")
    public static class OrderItemRequest {
        @Schema(description = "ID of the menu item", example = "1")
        private Long menuItemId;
        
        @Schema(description = "Quantity ordered", example = "2")
        private Integer quantity;
        
        @Schema(description = "Price per unit at the time of order", example = "15.00")
        private Double priceAtOrder;
    }
}
