package com.b4code.backend.modules.guest.rest;

import com.b4code.backend.modules.staff.entity.Order;
import com.b4code.backend.modules.staff.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GuestOrderController {

    private final OrderRepository orderRepository;
    private final com.b4code.backend.modules.staff.repository.MenuItemRepository menuItemRepository;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody com.b4code.backend.modules.guest.dto.OrderRequest request) {
        log.info("Placing new order for guest: {} at property: {}", request.getGuestId(), request.getPropertyId());
        
        Order order = new Order();
        order.setPropertyId(request.getPropertyId());
        order.setGuestId(request.getGuestId());
        order.setRoomNumber(request.getRoomNumber());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(request.getStatus() != null ? request.getStatus() : "NEW");

        if (request.getItems() != null) {
            for (com.b4code.backend.modules.guest.dto.OrderRequest.OrderItemRequest itemReq : request.getItems()) {
                com.b4code.backend.modules.staff.entity.OrderItem item = new com.b4code.backend.modules.staff.entity.OrderItem();
                item.setOrder(order);
                item.setQuantity(itemReq.getQuantity());
                item.setPriceAtOrder(itemReq.getPriceAtOrder());
                
                menuItemRepository.findById(itemReq.getMenuItemId()).ifPresent(item::setMenuItem);
                order.getItems().add(item);
            }
        }

        return ResponseEntity.ok(orderRepository.save(order));
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<Order>> getGuestOrderHistory(@PathVariable Long guestId) {
        log.info("Fetching order history for guest: {}", guestId);
        return ResponseEntity.ok(orderRepository.findByGuestIdOrderByCreatedAtDesc(guestId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
