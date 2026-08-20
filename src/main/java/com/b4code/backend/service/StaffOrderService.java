package com.b4code.backend.service;

import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.StaffOrderActionDto;
import com.b4code.backend.exception.OrderNotFoundException;
import com.b4code.backend.exception.StatusTransitionException;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.OrderStatusLog;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.OrderStatus;
import com.b4code.backend.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByProperty(Long propertyId, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        assertPropertyAccess(resolveCurrentUser(), propertyId);

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
        User actor = resolveCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        assertPropertyAccess(actor, order.getPropertyId());

        OrderStatus oldStatus = order.getStatus();

        validateTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedBy(actor.getEmail());
        Order saved = orderRepository.save(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(saved);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangedBy(actor.getEmail());
        orderStatusLogRepository.save(log);

        orderSseService.sendEvent(orderId, "status-update",
                java.util.Map.of("orderId", orderId, "status", newStatus.name()));
        orderSseService.sendPropertyEvent(saved.getPropertyId(), "status-update", saved);
        
        if (saved.getGuestId() != null) {
            userRepository.findById(saved.getGuestId()).ifPresent(guest -> {
                String msg = String.format("Your order #%d is now %s", saved.getId(), newStatus.name());
                notificationService.createNotification(guest, "Order Update", msg);
            });
        }

        return saved;
    }

    @Transactional
    public Order rejectOrder(Long orderId, StaffOrderActionDto actionDto) {
        if (actionDto == null || !Boolean.TRUE.equals(actionDto.getConfirm())) {
            throw new IllegalArgumentException("Explicit confirmation is required to reject an order.");
        }

        User actor = resolveCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        assertPropertyAccess(actor, order.getPropertyId());

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new StatusTransitionException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedBy(actor.getEmail());
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
        log.setChangedBy(actor.getEmail());
        orderStatusLogRepository.save(log);

        orderSseService.sendEvent(orderId, "status-update",
                java.util.Map.of("orderId", orderId, "status", OrderStatus.CANCELLED.name()));
        orderSseService.sendPropertyEvent(saved.getPropertyId(), "status-update", saved);
        
        if (saved.getGuestId() != null) {
            userRepository.findById(saved.getGuestId()).ifPresent(guest -> {
                String msg = String.format("Your order #%d was rejected by staff. Reason: %s", saved.getId(), actionDto.getReason() != null ? actionDto.getReason() : "None provided");
                notificationService.createNotification(guest, "Order Rejected", msg);
            });
        }

        return saved;
    }

    private User resolveCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        if (email == null) {
            throw new CustomException("Authentication required.", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found: " + email, HttpStatus.NOT_FOUND));
    }

    // STAFF accounts are scoped to a single property; OWNER/ADMIN aren't tied to user.propertyId so they bypass this check.
    private void assertPropertyAccess(User user, Long propertyId) {
        if (user.getRole() == UserRole.STAFF && !propertyId.equals(user.getPropertyId())) {
            throw new CustomException("You do not have access to this property's orders.", HttpStatus.FORBIDDEN);
        }
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
            case PAYMENT_PENDING:
                if (next == OrderStatus.ACCEPTED) {
                    throw new StatusTransitionException("Payment not completed, reject the order");
                }
                if (next == OrderStatus.PLACED || next == OrderStatus.CANCELLED) valid = true;
                break;
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
