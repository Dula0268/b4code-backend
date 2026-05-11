package com.b4code.backend.modules.guest.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long propertyId;
    private Long guestId;
    private String roomNumber;
    private Double totalAmount;
    private String status;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long menuItemId;
        private Integer quantity;
        private Double priceAtOrder;
    }
}
