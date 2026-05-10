package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.entity.Order;
import com.b4code.backend.modules.staff.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@Tag(name = "Staff: Order Management", description = "Endpoints for staff to manage guest orders and kitchen workflow")
public class StaffOrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get all orders for a property", description = "Returns all active and past orders for the specified property")
    public ResponseEntity<List<Order>> getOrdersByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(orderRepository.findByPropertyId(propertyId));
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Accept an order", description = "Staff acknowledges the order. Status changes from NEW to ACCEPTED.")
    public ResponseEntity<Order> acceptOrder(@PathVariable Long id) {
        return updateStatus(id, "ACCEPTED");
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject/Cancel an order", description = "Staff rejects the order (e.g. out of stock). Status changes to CANCELLED.")
    public ResponseEntity<Order> rejectOrder(@PathVariable Long id) {
        return updateStatus(id, "CANCELLED");
    }

    @PatchMapping("/{id}/ready")
    @Operation(summary = "Mark order as ready", description = "Kitchen has finished preparation. Status changes to READY.")
    public ResponseEntity<Order> markAsReady(@PathVariable Long id) {
        return updateStatus(id, "READY");
    }

    @PatchMapping("/{id}/deliver")
    @Operation(summary = "Mark order as delivered", description = "Order has been handed over to the guest. Status changes to DELIVERED.")
    public ResponseEntity<Order> markAsDelivered(@PathVariable Long id) {
        return updateStatus(id, "DELIVERED");
    }

    private ResponseEntity<Order> updateStatus(Long id, String status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }
}
