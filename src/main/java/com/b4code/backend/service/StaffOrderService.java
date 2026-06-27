package com.b4code.backend.service;

import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.dto.StaffOrderActionDto;
import com.b4code.backend.exception.OrderNotFoundException;
import com.b4code.backend.exception.StatusTransitionException;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.OrderStatusLog;
import com.b4code.backend.models.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.b4code.backend.dto.OrderResponse;
import com.b4code.backend.dto.OrderMapper;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StaffOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final OrderSseService orderSseService;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByProperty(Long propertyId, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders;
        if (status == null && startDate == null && endDate == null) {
            orders = orderRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId, pageable);
        } else {
            orders = orderRepository.findStaffOrders(propertyId, status, startDate, endDate, pageable);
        }
        return orders.map(OrderMapper::toResponse);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        
        validateTransition(oldStatus, newStatus);
        
        order.setStatus(newStatus);
        order.setUpdatedBy("STAFF");
        Order saved = orderRepository.save(order);
        
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(saved);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangedBy("STAFF");
        orderStatusLogRepository.save(log);
        
        orderSseService.sendEvent(orderId, "status-update",
                java.util.Map.of("orderId", orderId, "status", newStatus.name()));
        orderSseService.sendPropertyEvent(saved.getPropertyId(), "status-update", saved);
        
        return saved;
    }

    @Transactional
    public Order rejectOrder(Long orderId, StaffOrderActionDto actionDto) {
        if (actionDto == null || !Boolean.TRUE.equals(actionDto.getConfirm())) {
            throw new IllegalArgumentException("Explicit confirmation is required to reject an order.");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new StatusTransitionException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedBy("STAFF");
        if (actionDto.getReason() != null) {
            order.setStaffNotes(order.getStaffNotes() != null 
                    ? order.getStaffNotes() + "\nRejection reason: " + actionDto.getReason() 
                    : "Rejection reason: " + actionDto.getReason());
        }
        Order saved = orderRepository.save(order);
        
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(saved);
        log.setOldStatus(oldStatus);
        log.setNewStatus(OrderStatus.CANCELLED);
        log.setChangedBy("STAFF");
        orderStatusLogRepository.save(log);
        
        orderSseService.sendEvent(orderId, "status-update",
                java.util.Map.of("orderId", orderId, "status", OrderStatus.CANCELLED.name()));
        orderSseService.sendPropertyEvent(saved.getPropertyId(), "status-update", saved);
        
        return saved;
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return; // Idempotent transition, allow it
        }
        
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new StatusTransitionException("Cannot change status of a " + current + " order.");
        }
        
        boolean valid = false;
        switch (current) {
            case PLACED:
                if (next == OrderStatus.ACCEPTED || next == OrderStatus.CANCELLED) valid = true;
                break;
            case ACCEPTED:
                if (next == OrderStatus.IN_PROGRESS || next == OrderStatus.READY || next == OrderStatus.CANCELLED) valid = true;
                break;
            case IN_PROGRESS:
                if (next == OrderStatus.READY || next == OrderStatus.CANCELLED) valid = true;
                break;
            case READY:
                if (next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED) valid = true;
                break;
            default:
                break;
        }
        
        if (!valid) {
            throw new StatusTransitionException("Invalid transition from " + current + " to " + next);
        }
    }
}
