package com.b4code.backend.rest;

import com.b4code.backend.models.Order;
import com.b4code.backend.models.enums.OrderStatus;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.service.OrderSseService;
import com.b4code.backend.service.StaffOrderService;
import com.b4code.backend.dto.OrderResponse;
import com.b4code.backend.dto.StaffOrderActionDto;
import com.b4code.backend.dto.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Staff Order Management", description = "Endpoints for staff to manage orders")
public class StaffOrderController {

    private final OrderRepository orderRepository;
    private final StaffOrderService staffOrderService;
    private final OrderSseService orderSseService;

    @Operation(summary = "Get paginated orders by property", description = "Fetch orders with optional filtering by status and date")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByProperty(
            @PathVariable Long propertyId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching orders for property: {}", propertyId);
        LocalDateTime startDate = date != null ? date.atStartOfDay() : null;
        LocalDateTime endDate = date != null ? date.plusDays(1).atStartOfDay() : null;
        
        return ResponseEntity.ok(staffOrderService.getOrdersByProperty(propertyId, status, startDate, endDate, pageable));
    }

    @Operation(summary = "Accept an order", description = "Changes order status to ACCEPTED")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderMapper.toResponse(staffOrderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED)));
    }

    @Operation(summary = "Reject/Cancel an order", description = "Requires explicit confirmation to cancel an order")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long orderId, @RequestBody StaffOrderActionDto actionDto) {
        return ResponseEntity.ok(OrderMapper.toResponse(staffOrderService.rejectOrder(orderId, actionDto)));
    }

    @Operation(summary = "Mark order as ready", description = "Changes order status to READY")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PatchMapping("/{orderId}/ready")
    public ResponseEntity<OrderResponse> markAsReady(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderMapper.toResponse(staffOrderService.updateOrderStatus(orderId, OrderStatus.READY)));
    }

    @Operation(summary = "Mark order as delivered", description = "Changes order status to DELIVERED")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponse> markAsDelivered(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderMapper.toResponse(staffOrderService.updateOrderStatus(orderId, OrderStatus.DELIVERED)));
    }

    @GetMapping(path = "/property/{propertyId}/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamPropertyOrders(@PathVariable Long propertyId) {
        return orderSseService.addPropertyEmitter(propertyId);
    }
}
