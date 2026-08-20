package com.b4code.backend.rest;

import com.b4code.backend.service.OrderSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderNotificationController {

    private final OrderSseService orderSseService;

    @GetMapping(value = "/{orderId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderUpdates(@PathVariable Long orderId) {
        log.info("SSE stream requested for order: {}", orderId);
        return orderSseService.addEmitter(orderId);
    }
}
