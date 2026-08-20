package com.b4code.backend.service;

import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.dto.OrderRequest;
import com.b4code.backend.exception.MenuItemNotFoundException;
import com.b4code.backend.exception.StatusTransitionException;
import com.b4code.backend.models.MenuItem;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private OrderStatusLogRepository orderStatusLogRepository;

    @InjectMocks
    private GuestOrderService guestOrderService;

    private OrderRequest request;

    @BeforeEach
    void setUp() {
        request = new OrderRequest();
        request.setGuestSessionId("12345678-1234-1234-1234-123456789012");
        request.setPropertyId(1L);
        request.setGuestId(2L);
        request.setGuestName("John Doe");
    }

    @Test
    void placeOrder_Success() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setMenuItemId(10L);
        request.setItems(Collections.singletonList(itemReq));

        MenuItem menuItem = new MenuItem();
        menuItem.setId(10L);
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
        
        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setStatus(OrderStatus.PLACED);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = guestOrderService.placeOrder(request);

        assertNotNull(result);
        assertEquals(OrderStatus.PLACED, result.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(orderStatusLogRepository).save(any());
    }

    @Test
    void placeOrder_InvalidSessionId() {
        request.setGuestSessionId("invalid-format");
        assertThrows(RuntimeException.class, () -> guestOrderService.placeOrder(request));
    }

    @Test
    void placeOrder_MenuItemNotFound() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setMenuItemId(99L);
        request.setItems(Collections.singletonList(itemReq));

        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MenuItemNotFoundException.class, () -> guestOrderService.placeOrder(request));
    }

    @Test
    void cancelOrder_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = guestOrderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderStatusLogRepository).save(any());
    }

    @Test
    void cancelOrder_FailsIfDelivered() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(StatusTransitionException.class, () -> guestOrderService.cancelOrder(1L));
    }
}
