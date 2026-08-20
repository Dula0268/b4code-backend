package com.b4code.backend.service;

import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.OrderResponse;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.OrderStatus;
import com.b4code.backend.models.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

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
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StaffOrderService staffOrderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setPropertyId(10L);
        order.setStatus(OrderStatus.PLACED);

        // resolveCurrentUser() reads the authenticated principal from the static
        // SecurityContextHolder rather than an injected dependency, so it must be
        // populated directly here instead of via @Mock/@InjectMocks.
        User staffUser = new User();
        staffUser.setEmail("staff@example.com");
        staffUser.setRole(UserRole.STAFF);
        staffUser.setPropertyId(10L);
        when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staffUser));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("staff@example.com", null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

        Order rejectedOrder = staffOrderService.rejectOrder(1L, actionDto);

        assertEquals(OrderStatus.CANCELLED, rejectedOrder.getStatus());
        assertEquals("Rejection reason: Item out of stock", rejectedOrder.getStaffNotes());
    }
}
