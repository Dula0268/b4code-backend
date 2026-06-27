package com.b4code.backend.flow;

import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.QRCodeRepository;
import com.b4code.backend.dao.ReviewRepository;
import com.b4code.backend.dto.OrderRequest;
import com.b4code.backend.dto.QRCodeGenerateRequest;
import com.b4code.backend.dto.ReviewDTO.CreateReviewRequest;
import com.b4code.backend.dto.StaffOrderActionDto;
import com.b4code.backend.models.MenuItem;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.QRCode;
import com.b4code.backend.models.Review;
import com.b4code.backend.models.enums.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class E2EOrderFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private MenuItemRepository menuItemRepository;

    @MockBean
    private OrderStatusLogRepository orderStatusLogRepository;

    @MockBean
    private QRCodeRepository qrCodeRepository;

    @MockBean
    private PropertyRepository propertyRepository;
    
    @MockBean
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setupMocks() {
        // Setup Property Mock
        Property property = new Property();
        property.setId(1L);
        Mockito.when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        // Setup MenuItem Mock
        MenuItem menuItem = new MenuItem();
        menuItem.setId(10L);
        menuItem.setPrice(new BigDecimal("15.0"));
        Mockito.when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));

        // Setup Order Save Mock
        Mockito.when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            if (order.getId() == null) order.setId(100L);
            if (order.getCreatedAt() == null) order.setCreatedAt(LocalDateTime.now());
            return order;
        });

        // Setup QR Save Mock
        Mockito.when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(i -> {
            QRCode qr = i.getArgument(0);
            if (qr.getId() == null) qr.setId(50L);
            return qr;
        });
        
        // Setup Review Save Mock
        Mockito.when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review review = i.getArgument(0);
            if (review.getId() == null) review.setId(200L);
            return review;
        });
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void executeFullUserFlow() throws Exception {
        // 1. QR Generation for a Table
        QRCodeGenerateRequest qrReqTable = new QRCodeGenerateRequest();
        qrReqTable.setPropertyId(1L);
        qrReqTable.setType("TABLE");
        qrReqTable.setName("Table 5");

        mockMvc.perform(post("/api/qr/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qrReqTable)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Table 5"));

        // 2. Guest places order as Table
        OrderRequest tableOrderReq = new OrderRequest();
        tableOrderReq.setPropertyId(1L);
        tableOrderReq.setGuestSessionId("12345678-1234-1234-1234-123456789012");
        tableOrderReq.setLocation("Table 5");
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setMenuItemId(10L);
        itemReq.setQuantity(2);
        tableOrderReq.setItems(Collections.singletonList(itemReq));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tableOrderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Table 5"))
                .andExpect(jsonPath("$.status").value("PLACED"));

        // 3. Staff accepts order
        Order mockedTableOrder = new Order();
        mockedTableOrder.setId(100L);
        mockedTableOrder.setPropertyId(1L);
        mockedTableOrder.setStatus(OrderStatus.PLACED);
        Mockito.when(orderRepository.findById(100L)).thenReturn(Optional.of(mockedTableOrder));

        mockMvc.perform(patch("/api/staff/orders/100/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 4. Staff delivers order
        mockedTableOrder.setStatus(OrderStatus.ACCEPTED);
        mockMvc.perform(patch("/api/staff/orders/100/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
                
        mockedTableOrder.setStatus(OrderStatus.READY);
        mockMvc.perform(patch("/api/staff/orders/100/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        // 5. Guest writes a review
        CreateReviewRequest reviewReq = new CreateReviewRequest();
        reviewReq.setBookingId(1L);
        reviewReq.setOverallRating(5);
        reviewReq.setComment("Amazing service at Table 5!");

        mockMvc.perform(post("/api/guest/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.overallRating").value(5));

        // 6. Ordering as Room
        OrderRequest roomOrderReq = new OrderRequest();
        roomOrderReq.setPropertyId(1L);
        roomOrderReq.setGuestSessionId("87654321-4321-4321-4321-210987654321");
        roomOrderReq.setLocation("Room 101");
        roomOrderReq.setItems(Collections.singletonList(itemReq));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomOrderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Room 101"))
                .andExpect(jsonPath("$.status").value("PLACED"));
    }
}
