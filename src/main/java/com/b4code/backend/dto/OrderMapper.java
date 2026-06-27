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
        resp.setStatus(order.getStatus());
        resp.setGuestInstructions(order.getGuestInstructions());
        resp.setPaymentMethod(order.getPaymentMethod());
        resp.setStaffNotes(order.getStaffNotes());
        resp.setCreatedAt(order.getCreatedAt());
        
        if (order.getItems() != null) {
            resp.setItems(order.getItems().stream().map(item -> {
                OrderResponse.OrderItemResponse itemResp = new OrderResponse.OrderItemResponse();
                itemResp.setId(item.getId());
                if (item.getMenuItem() != null) {
                    itemResp.setMenuItemId(item.getMenuItem().getId());
                }
                itemResp.setQuantity(item.getQuantity());
                itemResp.setPriceAtOrder(item.getPriceAtOrder());
                itemResp.setNote(item.getNote());
                return itemResp;
            }).collect(Collectors.toList()));
        }
        return resp;
    }
}
