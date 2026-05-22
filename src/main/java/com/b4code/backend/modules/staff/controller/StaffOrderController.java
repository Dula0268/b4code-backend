package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.entity.Order;
import com.b4code.backend.modules.staff.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Staff: Order Management", description = "Endpoints for staff to manage and process guest orders")
public class StaffOrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get orders by property", description = "Returns a list of all orders for a specific property, sorted by creation date descending")
    public ResponseEntity<List<Order>> getOrdersByProperty(@PathVariable Long propertyId) {
        log.info("Fetching orders for property: {}", propertyId);
        List<Order> orders = orderRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/accept")
    @Operation(summary = "Accept an order", description = "Updates order status to PREPARING")
    @ApiResponse(responseCode = "200", description = "Order accepted successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<Order> acceptOrder(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, "PREPARING");
    }

    @PatchMapping("/{orderId}/reject")
    @Operation(summary = "Reject/Cancel an order", description = "Updates order status to CANCELLED")
    public ResponseEntity<Order> rejectOrder(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, "CANCELLED");
    }

    @PatchMapping("/{orderId}/ready")
    @Operation(summary = "Mark order as ready", description = "Updates order status to READY for pickup/delivery")
    public ResponseEntity<Order> markAsReady(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, "READY");
    }

    @PatchMapping("/{orderId}/deliver")
    @Operation(summary = "Mark order as delivered", description = "Updates order status to DELIVERED")
    public ResponseEntity<Order> markAsDelivered(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, "DELIVERED");
    }

    private ResponseEntity<Order> updateOrderStatus(Long orderId, String status) {
        log.info("Updating order {} status to {}", orderId, status);
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    return ResponseEntity.ok(orderRepository.save(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
