package com.b4code.backend.rest;

import com.b4code.backend.service.GuestOrderService;
import com.b4code.backend.service.OrderSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationController {

    private final OrderSseService orderSseService;
    private final GuestOrderService guestOrderService;

    @Operation(summary = "Stream order updates", description = "Opens an SSE stream for a single order. Requires the guestSessionId that placed the order (query param) to prove ownership.")
    @GetMapping(value = "/{orderId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderUpdates(
            @PathVariable Long orderId,
            @Parameter(description = "Guest session ID (UUID) that placed this order; must match or the stream is refused", required = true)
            @RequestParam String guestSessionId) {
        log.info("SSE stream requested for order: {}", orderId);
        // Verify ownership before opening the emitter (same guestSessionId gate as the REST endpoints).
        guestOrderService.getOrderById(orderId, guestSessionId);
        return orderSseService.addEmitter(orderId);
    }
}
