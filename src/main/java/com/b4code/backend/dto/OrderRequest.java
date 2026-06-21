package com.b4code.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long propertyId;
    private Long guestId;
    private String roomNumber;
    private Long tableId;
    private String tableNumber;
    private String guestName;
    private String guestPhone;
    private Double totalAmount;
    private String status;
    private String guestInstructions;
    private String paymentMethod;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long menuItemId;
        private Integer quantity;
        private Double priceAtOrder;
    }
}

