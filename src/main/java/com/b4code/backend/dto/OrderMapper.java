package com.b4code.backend.dto;

import com.b4code.backend.models.Order;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderResponse toResponse(Order order) {
        if (order == null) return null;
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setPropertyId(order.getPropertyId());
        resp.setGuestId(order.getGuestId());
        resp.setLocation(order.getLocation());
        resp.setGuestName(order.getGuestName());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setSubtotalAmount(order.getSubtotalAmount());
        resp.setServiceChargeAmount(order.getServiceChargeAmount());
        resp.setTaxAmount(order.getTaxAmount());
        resp.setDiscountAmount(order.getDiscountAmount());
        resp.setServiceChargeRate(order.getServiceChargeRate());
        resp.setTaxRate(order.getTaxRate());
        resp.setStatus(order.getStatus());
        resp.setGuestInstructions(order.getGuestInstructions());
        resp.setPaymentMethod(order.getPaymentMethod());
        resp.setStaffNotes(order.getStaffNotes());
        resp.setCreatedAt(order.getCreatedAt());
        resp.setCancelledBy(order.getCancelledBy());
        resp.setCancelledAt(order.getCancelledAt());
        resp.setRefundStatus(order.getRefundStatus());
        resp.setRefundAmount(order.getRefundAmount());
        resp.setRefundReference(order.getRefundReference());
        resp.setRefundedAt(order.getRefundedAt());
        
        if (order.getItems() != null) {
            resp.setItems(order.getItems().stream().map(item -> {
                OrderResponse.OrderItemResponse itemResp = new OrderResponse.OrderItemResponse();
                itemResp.setId(item.getId());
                if (item.getMenuItem() != null) {
                    OrderResponse.MenuItemResponse menuResp = new OrderResponse.MenuItemResponse();
                    menuResp.setId(item.getMenuItem().getId());
                    menuResp.setName(item.getMenuItem().getName());
                    menuResp.setTag(item.getMenuItem().getTag());
                    itemResp.setMenuItem(menuResp);
                }
                itemResp.setQuantity(item.getQuantity());
                itemResp.setPriceAtOrder(item.getPriceAtOrder());
                itemResp.setLineTotal(resolveLineTotal(item));
                itemResp.setNote(item.getNote());
                return itemResp;
            }).collect(Collectors.toList()));
        }

        backfillLegacyBreakdown(resp);
        return resp;
    }

    private static Double resolveLineTotal(com.b4code.backend.models.OrderItem item) {
        if (item.getLineTotal() != null) return item.getLineTotal();
        // Legacy row (persisted before line_total existed): derive it once here, server-side,
        // so clients still never multiply prices themselves.
        double price = item.getPriceAtOrder() != null ? item.getPriceAtOrder() : 0d;
        int qty = item.getQuantity() != null ? item.getQuantity() : 0;
        return round2(price * qty);
    }

    /**
     * Orders placed before the breakdown columns existed only have totalAmount. Derive the
     * missing components here — still server-side, still one authoritative answer that both
     * the guest and staff UIs receive — instead of letting each client invent its own formula.
     */
    private static void backfillLegacyBreakdown(OrderResponse resp) {
        if (resp.getSubtotalAmount() != null) return;

        double subtotal = 0d;
        if (resp.getItems() != null) {
            for (OrderResponse.OrderItemResponse item : resp.getItems()) {
                subtotal += item.getLineTotal() != null ? item.getLineTotal() : 0d;
            }
        }
        subtotal = round2(subtotal);
        double total = resp.getTotalAmount() != null ? resp.getTotalAmount() : subtotal;
        double charges = round2(Math.max(0d, total - subtotal));

        resp.setSubtotalAmount(subtotal);
        // The legacy total bundled service charge and tax together and we can no longer tell
        // them apart, so report the whole uplift as service charge rather than guessing a split.
        resp.setServiceChargeAmount(charges);
        resp.setTaxAmount(0d);
        resp.setDiscountAmount(0d);
        resp.setTotalAmount(round2(total));
    }

    private static double round2(double value) {
        return java.math.BigDecimal.valueOf(value)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }
}
