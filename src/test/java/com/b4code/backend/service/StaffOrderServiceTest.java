package com.b4code.backend.service;

import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.dto.OrderResponse;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StaffOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusLogRepository orderStatusLogRepository;

    @Mock
    private OrderSseService orderSseService;

    @Mock
    private com.b4code.backend.dao.UserRepository userRepository;
    @Mock
    private PaymentService paymentService;


    @InjectMocks
    private StaffOrderService staffOrderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setPropertyId(10L);
        order.setStatus(OrderStatus.PLACED);

        com.b4code.backend.models.User mockUser = new com.b4code.backend.models.User();
        mockUser.setEmail("staff@test.com");
        mockUser.setRole(com.b4code.backend.models.enums.UserRole.STAFF);
        mockUser.setPropertyId(10L);

        when(userRepository.findByEmail("staff@test.com")).thenReturn(java.util.Optional.of(mockUser));

        org.springframework.security.core.context.SecurityContext securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        org.springframework.security.core.Authentication authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("staff@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetOrdersByProperty_IncomingNewOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> mockPage = new PageImpl<>(List.of(order));

        when(orderRepository.findStaffOrders(eq(10L), eq(OrderStatus.PLACED), any(), any(), eq(pageable)))
                .thenReturn(mockPage);

        Page<OrderResponse> result = staffOrderService.getOrdersByProperty(10L, OrderStatus.PLACED, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(OrderStatus.PLACED, result.getContent().get(0).getStatus());
    }

    @Test
    void testRejectOrder() {
        com.b4code.backend.dto.StaffOrderActionDto actionDto = new com.b4code.backend.dto.StaffOrderActionDto();
        actionDto.setConfirm(true);
        actionDto.setReason("Item out of stock");

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(paymentService.refundFoodOrderPayment(1L))
                .thenReturn(new PaymentService.FoodOrderRefundResult(true, 2500.0, "ORDER-ABC123"));

        Order rejectedOrder = staffOrderService.rejectOrder(1L, actionDto);

        assertEquals(OrderStatus.CANCELLED, rejectedOrder.getStatus());
        assertEquals("Rejection reason: Item out of stock", rejectedOrder.getStaffNotes());
        assertEquals(com.b4code.backend.models.enums.OrderActorType.STAFF, rejectedOrder.getCancelledBy());
        assertEquals(com.b4code.backend.models.enums.OrderRefundStatus.REFUNDED, rejectedOrder.getRefundStatus());
        assertEquals(2500.0, rejectedOrder.getRefundAmount());
    }
}
