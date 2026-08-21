package com.b4code.backend.rest;

import com.b4code.backend.dto.OrderMessageDto;
import com.b4code.backend.dto.OrderMessageRequest;
import com.b4code.backend.service.OrderMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public, unauthenticated guest-facing endpoints for messaging staff about an order.
 * Ownership is proven via guestSessionId (query param), mirroring GuestOrderController.
 */
@RestController
@RequestMapping("/api/orders/{orderId}/messages")
@RequiredArgsConstructor
public class GuestOrderMessageController {

    private final OrderMessageService orderMessageService;

    @GetMapping
    public ResponseEntity<List<OrderMessageDto>> getMessages(
            @PathVariable Long orderId,
            @RequestParam String guestSessionId) {
        return ResponseEntity.ok(orderMessageService.getMessagesForGuest(orderId, guestSessionId));
    }

    @PostMapping
    public ResponseEntity<OrderMessageDto> sendMessage(
            @PathVariable Long orderId,
            @RequestParam String guestSessionId,
            @Valid @RequestBody OrderMessageRequest request) {
        return ResponseEntity.ok(orderMessageService.sendMessageAsGuest(orderId, guestSessionId, request.getContent()));
    }
}
