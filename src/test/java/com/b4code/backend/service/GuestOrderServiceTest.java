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
    @Mock
    private com.b4code.backend.dao.PropertyRepository propertyRepository;
    @Mock
    private com.b4code.backend.dao.UserRepository userRepository;
    @Mock
    private com.b4code.backend.service.NotificationService notificationService;
    @Mock
    private com.b4code.backend.service.OrderSseService orderSseService;
    @Mock
    private com.b4code.backend.service.PaymentService paymentService;

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
        menuItem.setPropertyId(1L);
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
        order.setGuestSessionId("test-session-id");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentService.refundFoodOrderPayment(1L))
                .thenReturn(PaymentService.FoodOrderRefundResult.nothingToRefund());

        Order result = guestOrderService.cancelOrder(1L, "test-session-id");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderStatusLogRepository).save(any());
    }

    @Test
    void cancelOrder_RefundsPaidOrderAndRecordsGuestAsActor() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        order.setGuestSessionId("test-session-id");
        order.setPaymentMethod("card");
        order.setTotalAmount(4200.0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(paymentService.refundFoodOrderPayment(1L))
                .thenReturn(new PaymentService.FoodOrderRefundResult(true, 4200.0, "ORDER-9F2A1B"));

        Order result = guestOrderService.cancelOrder(1L, "test-session-id");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(com.b4code.backend.models.enums.OrderActorType.GUEST, result.getCancelledBy());
        assertNotNull(result.getCancelledAt());
        assertEquals(com.b4code.backend.models.enums.OrderRefundStatus.REFUNDED, result.getRefundStatus());
        // Authoritative amount comes from the payment record, not the caller.
        assertEquals(4200.0, result.getRefundAmount());
        assertEquals("ORDER-9F2A1B", result.getRefundReference());
        assertNotNull(result.getRefundedAt());
        verify(paymentService).refundFoodOrderPayment(1L);
    }

    @Test
    void cancelOrder_UnpaidOrderIsMarkedNotApplicable() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        order.setGuestSessionId("test-session-id");
        order.setPaymentMethod("cash");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(paymentService.refundFoodOrderPayment(1L))
                .thenReturn(PaymentService.FoodOrderRefundResult.nothingToRefund());

        Order result = guestOrderService.cancelOrder(1L, "test-session-id");

        assertEquals(com.b4code.backend.models.enums.OrderRefundStatus.NOT_APPLICABLE, result.getRefundStatus());
        assertNull(result.getRefundAmount());
    }

    /** A double-cancel must be a no-op: no second refund, no second audit row. */
    @Test
    void cancelOrder_IsIdempotent_DoesNotRefundTwice() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELLED);
        order.setGuestSessionId("test-session-id");
        order.setCancelledBy(com.b4code.backend.models.enums.OrderActorType.GUEST);
        order.setRefundStatus(com.b4code.backend.models.enums.OrderRefundStatus.REFUNDED);
        order.setRefundAmount(4200.0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = guestOrderService.cancelOrder(1L, "test-session-id");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(4200.0, result.getRefundAmount());
        verify(paymentService, never()).refundFoodOrderPayment(any());
        verify(orderStatusLogRepository, never()).save(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    /** A guest must not be able to cancel somebody else's order (IDOR guard). */
    @Test
    void cancelOrder_ForeignSessionIsRejectedWithoutRefund() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        order.setGuestSessionId("owner-session-id");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(com.b4code.backend.exceptions.CustomException.class,
                () -> guestOrderService.cancelOrder(1L, "attacker-session-id"));

        assertEquals(OrderStatus.PLACED, order.getStatus());
        verify(paymentService, never()).refundFoodOrderPayment(any());
    }

    /** Once the kitchen is cooking, only staff may cancel — no guest self-refund. */
    @Test
    void cancelOrder_FailsOnceInProgress() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setGuestSessionId("test-session-id");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(StatusTransitionException.class, () -> guestOrderService.cancelOrder(1L, "test-session-id"));
        verify(paymentService, never()).refundFoodOrderPayment(any());
    }

    @Test
    void cancelOrder_FailsIfDelivered() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.DELIVERED);
        order.setGuestSessionId("test-session-id");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(StatusTransitionException.class, () -> guestOrderService.cancelOrder(1L, "test-session-id"));
    }
}
