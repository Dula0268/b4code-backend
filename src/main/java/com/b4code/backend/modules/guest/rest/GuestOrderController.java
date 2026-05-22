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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Guest: Order Management", description = "Endpoints for guests to place and view orders")
public class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping
    @Operation(summary = "Place a new order", description = "Creates a new order for a guest at a specific property/table/room")
    @ApiResponse(responseCode = "200", description = "Order placed successfully")
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request) {
        log.info("Placing new order for guest: {} at property: {}", request.getGuestId(), request.getPropertyId());
        return ResponseEntity.ok(guestOrderService.placeOrder(request));
    }

    @GetMapping("/guest/{guestId}")
    @Operation(
        summary = "Get guest order history", 
        description = "Returns a paginated list of orders placed by a specific guest",
        parameters = {
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page number (0-indexed)", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Items per page", schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Sorting criteria (format: property,asc|desc)", schema = @Schema(type = "string", example = "createdAt,desc"))
        }
    )
    public ResponseEntity<Page<Order>> getGuestOrderHistory(
            @PathVariable Long guestId,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching order history for guest: {}, page: {}", guestId, pageable.getPageNumber());
        return ResponseEntity.ok(guestOrderService.getGuestOrderHistory(guestId, pageable));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(guestOrderService.getOrderById(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
