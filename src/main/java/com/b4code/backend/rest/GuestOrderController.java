package com.b4code.backend.rest;

import com.b4code.backend.models.Order;
import com.b4code.backend.service.GuestOrderService;
import com.b4code.backend.dto.OrderRequest;
import com.b4code.backend.dto.OrderResponse;
import com.b4code.backend.dto.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Guest Order Flow", description = "Endpoints for guests to place and manage orders")
public class GuestOrderController {

    private final GuestOrderService guestOrderService;
    private final com.b4code.backend.service.OrderSseService orderSseService;

    @Operation(summary = "Place a new order", description = "Validates payload and creates a new order for the guest")
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Placing new order for guest: {} at property: {}", request.getGuestId(), request.getPropertyId());
        Order savedOrder = guestOrderService.placeOrder(request);
        orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "new-order", savedOrder);
        return ResponseEntity.ok(OrderMapper.toResponse(savedOrder));
    }

    @Operation(summary = "Get guest order history", description = "Fetch paginated order history by guest ID")
    @GetMapping("/guest/{guestId}")
    public ResponseEntity<Page<OrderResponse>> getGuestOrderHistory(
            @PathVariable Long guestId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching order history for guest: {}, page: {}", guestId, pageable.getPageNumber());
        return ResponseEntity.ok(guestOrderService.getGuestOrderHistory(guestId, pageable).map(OrderMapper::toResponse));
    }

    @Operation(summary = "Get guest order history by session", description = "Fetch paginated order history by guest session ID")
    @GetMapping("/session/{guestSessionId}")
    public ResponseEntity<Page<OrderResponse>> getGuestOrderHistoryBySession(
            @PathVariable String guestSessionId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching order history for session: {}, page: {}", guestSessionId, pageable.getPageNumber());
        return ResponseEntity.ok(guestOrderService.getGuestOrderHistoryBySession(guestSessionId, pageable).map(OrderMapper::toResponse));
    }

    @Operation(summary = "Get order details", description = "Fetch order details by order ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderMapper.toResponse(guestOrderService.getOrderById(orderId)));
    }

    @Operation(summary = "Cancel an order", description = "Cancels a placed order if not yet delivered")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order: {}", orderId);
        return ResponseEntity.ok(OrderMapper.toResponse(guestOrderService.cancelOrder(orderId)));
    }
}

