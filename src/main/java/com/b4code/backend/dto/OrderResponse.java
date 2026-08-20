package com.b4code.backend.dto;

import com.b4code.backend.models.enums.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long propertyId;
    private Long guestId;
    private String location;
    private String guestName;
    private Double totalAmount;
    private OrderStatus status;
    private String guestInstructions;
    private String paymentMethod;
    private String staffNotes;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    @Data
    public static class OrderItemResponse {
        private Long id;
        private MenuItemResponse menuItem;
        private Integer quantity;
        private Double priceAtOrder;
        private String note;
    }

    @Data
    public static class MenuItemResponse {
        private Long id;
        private String name;
        private String tag;
    }
}
