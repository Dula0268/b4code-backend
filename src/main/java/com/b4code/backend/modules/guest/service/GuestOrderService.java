package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.OrderRequest;
import com.b4code.backend.modules.staff.entity.Order;
import com.b4code.backend.modules.staff.entity.OrderItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import com.b4code.backend.modules.staff.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setPropertyId(request.getPropertyId());
        order.setGuestId(request.getGuestId());
        order.setRoomNumber(request.getRoomNumber());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(request.getStatus() != null ? request.getStatus() : "NEW");

        if (request.getItems() != null) {
            for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setQuantity(itemReq.getQuantity());
                item.setPriceAtOrder(itemReq.getPriceAtOrder());
                
                menuItemRepository.findById(itemReq.getMenuItemId()).ifPresent(item::setMenuItem);
                order.getItems().add(item);
            }
        }

        return orderRepository.save(order);
    }

    public Page<Order> getGuestOrderHistory(Long guestId, Pageable pageable) {
        return orderRepository.findByGuestIdOrderByCreatedAtDesc(guestId, pageable);
    }
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }
}
