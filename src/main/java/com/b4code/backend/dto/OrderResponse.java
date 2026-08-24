package com.b4code.backend.dto;

import com.b4code.backend.models.enums.OrderStatus;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long propertyId;
    private Long guestId;
    private String location;
    private String guestName;
    // ─── Authoritative money breakdown (server-computed, server-persisted) ───
    // Added additively; existing clients that only read totalAmount keep working.
    private Double subtotalAmount;
    private Double serviceChargeAmount;
    private Double taxAmount;
    private Double discountAmount;
    private Double serviceChargeRate;
    private Double taxRate;
    private Double totalAmount;
    private OrderStatus status;
    private String guestInstructions;
    private String paymentMethod;
    private String staffNotes;
    private Instant createdAt;
    // ─── Cancellation attribution + refund outcome (additive) ───
    private com.b4code.backend.models.enums.OrderActorType cancelledBy;
    private Instant cancelledAt;
    private com.b4code.backend.models.enums.OrderRefundStatus refundStatus;
    private Double refundAmount;
    private String refundReference;
    private Instant refundedAt;
    private List<OrderItemResponse> items;

    @Data
    public static class OrderItemResponse {
        private Long id;
        private MenuItemResponse menuItem;
        private Integer quantity;
        private Double priceAtOrder;
        private Double lineTotal;
        private String note;
    }

    @Data
    public static class MenuItemResponse {
        private Long id;
        private String name;
        private String tag;
    }
}
