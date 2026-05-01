package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.dto.OrderDTO;
import com.b4code.backend.modules.staff.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final OrderService orderService;

    @GetMapping("/orders/queue/{propertyId}")
    public ResponseEntity<List<OrderDTO>> getOrderQueue(@PathVariable Long propertyId) {
        // Get NEW and PREPARING orders (active order queue)
        List<OrderDTO> newOrders = orderService.getOrdersByPropertyIdAndStatus(propertyId, "NEW");
        List<OrderDTO> preparingOrders = orderService.getOrdersByPropertyIdAndStatus(propertyId, "PREPARING");
        
        newOrders.addAll(preparingOrders);
        return ResponseEntity.ok(newOrders);
    }

    @GetMapping("/orders/property/{propertyId}")
    public ResponseEntity<List<OrderDTO>> getPropertyOrders(@PathVariable Long propertyId) {
        List<OrderDTO> orders = orderService.getOrdersByPropertyId(propertyId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/orders/{orderId}/accept")
    public ResponseEntity<OrderDTO> acceptOrder(@PathVariable Long orderId) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, "PREPARING");
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/orders/{orderId}/ready")
    public ResponseEntity<OrderDTO> markOrderReady(@PathVariable Long orderId) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, "READY");
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/orders/{orderId}/deliver")
    public ResponseEntity<OrderDTO> deliverOrder(@PathVariable Long orderId) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, "DELIVERED");
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/orders/{orderId}/reject")
    public ResponseEntity<OrderDTO> rejectOrder(@PathVariable Long orderId) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, "CANCELLED");
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/dashboard/stats/{propertyId}")
    public ResponseEntity<StaffDashboardStats> getDashboardStats(@PathVariable Long propertyId) {
        List<OrderDTO> newOrders = orderService.getOrdersByPropertyIdAndStatus(propertyId, "NEW");
        List<OrderDTO> preparingOrders = orderService.getOrdersByPropertyIdAndStatus(propertyId, "PREPARING");
        List<OrderDTO> readyOrders = orderService.getOrdersByPropertyIdAndStatus(propertyId, "READY");
        List<OrderDTO> deliveredOrders = orderService.getOrdersByPropertyIdAndStatus(propertyId, "DELIVERED");
        
        StaffDashboardStats stats = new StaffDashboardStats();
        stats.setPendingOrders(newOrders.size());
        stats.setPreparingOrders(preparingOrders.size());
        stats.setReadyOrders(readyOrders.size());
        stats.setDeliveredOrders(deliveredOrders.size());
        stats.setTotalOrders(newOrders.size() + preparingOrders.size() + readyOrders.size() + deliveredOrders.size());
        
        return ResponseEntity.ok(stats);
    }

    @lombok.Data
    public static class StaffDashboardStats {
        private Integer pendingOrders;
        private Integer preparingOrders;
        private Integer readyOrders;
        private Integer deliveredOrders;
        private Integer totalOrders;
    }
}
