package com.b4code.backend.service;

import com.b4code.backend.models.Order;
import com.b4code.backend.models.OrderItem;
import com.b4code.backend.dto.OrderRequest;
import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.exception.MenuItemNotFoundException;
import com.b4code.backend.exception.OrderNotFoundException;
import com.b4code.backend.exception.DuplicateOrderException;
import com.b4code.backend.exception.StatusTransitionException;
import com.b4code.backend.models.OrderStatusLog;
import com.b4code.backend.models.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;

    @Transactional
    public Order placeOrder(OrderRequest request) {
        if (request.getGuestSessionId() == null || !request.getGuestSessionId().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new RuntimeException("Invalid guest session ID format");
        }
        
        Order order = new Order();
        order.setPropertyId(request.getPropertyId());
        order.setGuestId(request.getGuestId());
        order.setGuestSessionId(request.getGuestSessionId());
        order.setGuestName(request.getGuestName());
        order.setGuestPhone(request.getGuestPhone());
        order.setLocation(request.getLocation());
        order.setTotalAmount(request.getTotalAmount());
        OrderStatus status = request.getStatus();
        if (status == null) {
            status = OrderStatus.PLACED;
        }
        order.setStatus(status);
        order.setGuestInstructions(request.getGuestInstructions());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCreatedBy(request.getGuestName() != null ? request.getGuestName() : "GUEST");
        order.setUpdatedBy(request.getGuestName() != null ? request.getGuestName() : "GUEST");

        if (request.getItems() != null) {
            for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setQuantity(itemReq.getQuantity());
                item.setPriceAtOrder(itemReq.getPriceAtOrder());
                item.setNote(itemReq.getNote());
                
                item.setMenuItem(menuItemRepository.findById(itemReq.getMenuItemId())
                        .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found with ID: " + itemReq.getMenuItemId())));
                order.getItems().add(item);
            }
        }

        Order savedOrder = orderRepository.save(order);
        
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(savedOrder);
        log.setOldStatus(null);
        log.setNewStatus(savedOrder.getStatus());
        log.setChangedBy(request.getGuestName() != null ? request.getGuestName() : "GUEST");
        orderStatusLogRepository.save(log);

        return savedOrder;
    }

    public Page<Order> getGuestOrderHistory(Long guestId, Pageable pageable) {
        return orderRepository.findByGuestIdOrderByCreatedAtDesc(guestId, pageable);
    }
    
    public Page<Order> getGuestOrderHistoryBySession(String guestSessionId, Pageable pageable) {
        return orderRepository.findByGuestSessionIdOrderByCreatedAtDesc(guestSessionId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new StatusTransitionException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedBy(order.getGuestName() != null ? order.getGuestName() : "GUEST");
        Order savedOrder = orderRepository.save(order);
        
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(savedOrder);
        log.setOldStatus(oldStatus);
        log.setNewStatus(OrderStatus.CANCELLED);
        log.setChangedBy(order.getGuestName() != null ? order.getGuestName() : "GUEST");
        orderStatusLogRepository.save(log);
        
        return savedOrder;
    }
}
