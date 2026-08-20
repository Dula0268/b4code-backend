package com.b4code.backend.rest;

import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Order;
import com.b4code.backend.service.GuestOrderService;
import com.b4code.backend.dto.OrderRequest;
import com.b4code.backend.dto.OrderResponse;
import com.b4code.backend.dto.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Guest Order Flow", description = "Endpoints for guests to place and manage orders")
public class GuestOrderController {

    private final GuestOrderService guestOrderService;
    private final com.b4code.backend.service.OrderSseService orderSseService;
    private final Environment environment;

    @Operation(summary = "Place a new order", description = "Validates payload and creates a new order for the guest")
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Placing new order for guest: {} at property: {}", request.getGuestId(), request.getPropertyId());
        Order savedOrder = guestOrderService.placeOrder(request);
        OrderResponse response = OrderMapper.toResponse(savedOrder);
        // Only broadcast to staff when payment is complete (not for pending online payments)
        if (savedOrder.getStatus() != com.b4code.backend.models.enums.OrderStatus.PAYMENT_PENDING) {
            orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "new-order", response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get guest order history", description = "Fetch paginated order history by guest ID. Requires the caller's guestSessionId to prove ownership.")
    @GetMapping("/guest/{guestId}")
    public ResponseEntity<Page<OrderResponse>> getGuestOrderHistory(
            @PathVariable Long guestId,
            @Parameter(description = "Guest session ID (UUID) established when the order(s) were placed; required to prove the caller owns this history")
            @RequestParam String guestSessionId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching order history for guest: {}, page: {}", guestId, pageable.getPageNumber());
        return ResponseEntity.ok(guestOrderService.getGuestOrderHistory(guestId, guestSessionId, pageable).map(OrderMapper::toResponse));
    }

    @Operation(summary = "Get guest order history by session", description = "Fetch paginated order history by guest session ID")
    @GetMapping("/session/{guestSessionId}")
    public ResponseEntity<Page<OrderResponse>> getGuestOrderHistoryBySession(
            @PathVariable String guestSessionId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching order history for session: {}, page: {}", guestSessionId, pageable.getPageNumber());
        return ResponseEntity.ok(guestOrderService.getGuestOrderHistoryBySession(guestSessionId, pageable).map(OrderMapper::toResponse));
    }

    @Operation(summary = "Get order details", description = "Fetch order details by order ID. Requires the guestSessionId that placed the order (query param) to prove ownership.")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @Parameter(description = "Guest session ID (UUID) that placed this order; must match or the order is treated as not found", required = true)
            @RequestParam String guestSessionId) {
        return ResponseEntity.ok(OrderMapper.toResponse(guestOrderService.getOrderById(orderId, guestSessionId)));
    }

    @Operation(summary = "Cancel an order", description = "Cancels a placed order if not yet delivered. Requires the guestSessionId that placed the order (query param) to prove ownership.")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @Parameter(description = "Guest session ID (UUID) that placed this order; must match or the order is treated as not found", required = true)
            @RequestParam String guestSessionId) {
        log.info("Cancelling order: {}", orderId);
        return ResponseEntity.ok(OrderMapper.toResponse(guestOrderService.cancelOrder(orderId, guestSessionId)));
    }

    /**
     * DEV-ONLY payment simulator. It flips an order straight to "paid" with no real payment
     * verification, bypassing the actual PayHere webhook flow (see PaymentService/PaymentController#notify).
     * This must NEVER be reachable in production - it is gated to the "dev" Spring profile
     * (mirroring the convention used by TestController) and returns 404 otherwise.
     */
    @Operation(summary = "Simulate payment webhook", description = "DEV ONLY: confirms an online payment and transitions it to PLACED. Disabled outside the 'dev' profile.")
    @PostMapping("/{orderId}/simulate-payment")
    public ResponseEntity<OrderResponse> simulatePayment(@PathVariable Long orderId) {
        if (!environment.acceptsProfiles(Profiles.of("dev"))) {
            throw new CustomException("Not found", HttpStatus.NOT_FOUND);
        }
        log.info("Simulating successful payment for order: {}", orderId);
        Order savedOrder = guestOrderService.confirmPayment(orderId);
        OrderResponse response = OrderMapper.toResponse(savedOrder);
        // Broadcast the new order since it's now officially placed
        orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "new-order", response);
        return ResponseEntity.ok(response);
    }
}
