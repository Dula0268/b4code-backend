package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.OrderRequest;
import com.b4code.backend.modules.staff.entity.Order;
import com.b4code.backend.modules.staff.entity.OrderItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import com.b4code.backend.modules.staff.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "propertyId", "guestId", "roomNumber", "totalAmount", "status", "createdAt"
    );

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
        Pageable sanitizedPageable = sanitizePageable(pageable);
        return orderRepository.findByGuestIdOrderByCreatedAtDesc(guestId, sanitizedPageable);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> validOrders = pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT_PROPERTIES.contains(order.getProperty()))
                .collect(Collectors.toList());

        if (validOrders.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(validOrders));
    }
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }
}
