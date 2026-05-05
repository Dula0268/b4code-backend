package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.dto.OrderDTO;
import com.b4code.backend.modules.staff.service.OrderService;
import com.b4code.backend.modules.staff.repository.OrderRepository;
import com.b4code.backend.modules.staff.repository.StaffPropertyRepository;
import com.b4code.backend.modules.staff.entity.StaffProperty;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.admin.dao.AdminPropertyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffPropertyRepository staffPropertyRepository;
    private final AdminPropertyRepository propertyRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping("/properties/{staffId}")
    public ResponseEntity<List<Property>> getStaffProperties(@PathVariable Long staffId) {
        List<StaffProperty> mappings = staffPropertyRepository.findByStaffId(staffId);
        List<Long> propertyIds = mappings.stream()
                .map(StaffProperty::getPropertyId)
                .collect(Collectors.toList());
        List<Property> properties = propertyRepository.findAllById(propertyIds);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkStatus(@RequestParam Long staffId,
            @RequestParam Long propertyId) {

        Optional<StaffProperty> sp = staffPropertyRepository.findByStaffIdAndPropertyId(staffId, propertyId);

        if (sp.isPresent()) {
            return ResponseEntity.ok(sp.get().getStatus());
        } else {
            return ResponseEntity.ok("NOT_SELECTED");
        }
    }

    @PostMapping("/properties")
    public ResponseEntity<StaffProperty> assignProperty(@RequestBody StaffProperty staffProperty) {
        StaffProperty saved = staffPropertyRepository.save(staffProperty);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/select-property")
    public ResponseEntity<?> selectProperty(@RequestParam Long staffId,
            @RequestParam Long propertyId) {

        var existing = staffPropertyRepository
                .findByStaffIdAndPropertyId(staffId, propertyId);

        StaffProperty sp = existing.orElseGet(StaffProperty::new);

        sp.setStaffId(staffId);
        sp.setPropertyId(propertyId);
        sp.setStatus(StaffProperty.Status.PENDING);

        staffPropertyRepository.save(sp);

        return ResponseEntity.ok("Waiting for approval");
    }

    @DeleteMapping("/properties/{staffId}/{propertyId}")
    public ResponseEntity<Void> removeProperty(
            @PathVariable Long staffId,
            @PathVariable Long propertyId) {
        staffPropertyRepository.deleteByStaffIdAndPropertyId(staffId, propertyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders/queue/{propertyId}")
    public ResponseEntity<List<OrderDTO>> getOrderQueue(@PathVariable Long propertyId) {
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
