package com.b4code.backend.rest;

import com.b4code.backend.models.Order;
import com.b4code.backend.dao.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StaffOrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<Order>> getOrdersByProperty(@PathVariable Long propertyId) {
        log.info("Fetching orders for property: {}", propertyId);
        List<Order> orders = orderRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<Order> acceptOrder(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, "PREPARING");
    }

    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<Order> rejectOrder(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, "CANCELLED");
    }

    @PatchMapping("/{orderId}/ready")
    public ResponseEntity<Order> markAsReady(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, "READY");
    }

    @PatchMapping("/{orderId}/deliver")
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



