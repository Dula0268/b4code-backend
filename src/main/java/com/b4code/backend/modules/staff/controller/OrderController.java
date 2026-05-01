package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.dto.OrderDTO;
import com.b4code.backend.modules.staff.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByProperty(@PathVariable Long propertyId) {
        List<OrderDTO> orders = orderService.getOrdersByPropertyId(propertyId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/property/{propertyId}/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByPropertyAndStatus(
            @PathVariable Long propertyId,
            @PathVariable String status) {
        List<OrderDTO> orders = orderService.getOrdersByPropertyIdAndStatus(propertyId, status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByGuest(@PathVariable Long guestId) {
        List<OrderDTO> orders = orderService.getOrdersByGuestId(guestId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{id}/status/{newStatus}")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable String newStatus) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
