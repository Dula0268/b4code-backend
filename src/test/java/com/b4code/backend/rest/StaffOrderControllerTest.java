package com.b4code.backend.rest;

import com.b4code.backend.dto.StaffOrderActionDto;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.enums.OrderStatus;
import com.b4code.backend.service.StaffOrderService;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.service.OrderSseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StaffOrderController.class)
class StaffOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private StaffOrderService staffOrderService;

    @MockBean
    private OrderSseService orderSseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "STAFF")
    void acceptOrder_Success() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.ACCEPTED);

        when(staffOrderService.updateOrderStatus(1L, OrderStatus.ACCEPTED)).thenReturn(order);

        mockMvc.perform(patch("/api/staff/orders/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void rejectOrder_RequiresConfirmationBody() throws Exception {
        StaffOrderActionDto action = new StaffOrderActionDto();
        action.setConfirm(true);
        action.setReason("Out of stock");

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELLED);

        when(staffOrderService.rejectOrder(eq(1L), any(StaffOrderActionDto.class))).thenReturn(order);

        mockMvc.perform(post("/api/staff/orders/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void rejectOrder_Unauthorized() throws Exception {
        StaffOrderActionDto action = new StaffOrderActionDto();
        action.setConfirm(true);

        mockMvc.perform(post("/api/staff/orders/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isUnauthorized());
    }
}
