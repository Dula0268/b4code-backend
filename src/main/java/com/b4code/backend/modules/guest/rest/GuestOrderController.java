package com.b4code.backend.modules.guest.rest;

import com.b4code.backend.modules.staff.entity.Order;
import com.b4code.backend.modules.guest.service.GuestOrderService;
import com.b4code.backend.modules.guest.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request) {
        log.info("Placing new order for guest: {} at property: {}", request.getGuestId(), request.getPropertyId());
        return ResponseEntity.ok(guestOrderService.placeOrder(request));
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<Page<Order>> getGuestOrderHistory(
            @PathVariable Long guestId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching order history for guest: {}, page: {}", guestId, pageable.getPageNumber());
        return ResponseEntity.ok(guestOrderService.getGuestOrderHistory(guestId, pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(guestOrderService.getOrderById(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
