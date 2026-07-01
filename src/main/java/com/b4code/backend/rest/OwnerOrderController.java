package com.b4code.backend.rest;

import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.models.Order;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/owner/orders")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Owner — Orders", description = "Owner-scoped order management")
public class OwnerOrderController {

    private final OrderRepository orderRepository;

    // ─── Inner DTO ────────────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerOrderSummary {
        private Long id;
        private Long propertyId;
        private String guestName;
        private String guestPhone;
        private String roomNumber;
        private String tableNumber;
        private Double totalAmount;
        private String status;
        private LocalDateTime createdAt;
        private int itemCount;
    }

    // ─── Endpoints ────────────────────────────────────────────────────────────────

    /**
     * GET /api/owner/orders/property/{propertyId}?page=0&size=15
     * Returns paginated orders for a specific property, sorted by createdAt desc.
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<Page<OwnerOrderSummary>> getOrdersByProperty(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        log.info("Owner: fetching orders for property {}, page={}, size={}", propertyId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId, pageable);

        return ResponseEntity.ok(orderPage.map(this::toSummary));
    }

    /**
     * GET /api/owner/orders/all?page=0&size=15
     * Returns all orders paginated, sorted by createdAt desc.
     */
    @GetMapping("/all")
    public ResponseEntity<Page<OwnerOrderSummary>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        log.info("Owner: fetching all orders, page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAll(pageable);

        return ResponseEntity.ok(orderPage.map(this::toSummary));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────────

    private OwnerOrderSummary toSummary(Order order) {
        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        return OwnerOrderSummary.builder()
                .id(order.getId())
                .propertyId(order.getPropertyId())
                .guestName(order.getGuestName())
                .guestPhone(order.getGuestPhone())
                .roomNumber(order.getRoomNumber())
                .tableNumber(order.getTableNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .itemCount(itemCount)
                .build();
    }
}
