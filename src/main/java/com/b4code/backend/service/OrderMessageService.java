package com.b4code.backend.service;

import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.ActiveOrderConversationDto;
import com.b4code.backend.dto.OrderMessageDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.OrderItem;
import com.b4code.backend.models.OrderMessage;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.repository.OrderMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderMessageService {

    private static final Set<String> KITCHEN_STAFF_ROLES = Set.of("Kitchen Staff", "Staff Admin");

    private final OrderMessageRepository orderMessageRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderSseService orderSseService;

    @Transactional
    public OrderMessageDto sendMessageAsGuest(Long orderId, String guestSessionId, String content) {
        Order order = findOrderOwnedBySession(orderId, guestSessionId);
        return sendMessage(order, guestSessionId, "GUEST", content);
    }

    @Transactional
    public OrderMessageDto sendMessageAsStaff(Long orderId, String staffEmail, String content) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found", HttpStatus.NOT_FOUND));
        User staff = assertKitchenStaffAccess(staffEmail, order.getPropertyId());
        return sendMessage(order, staff.getEmail(), "STAFF", content);
    }

    private OrderMessageDto sendMessage(Order order, String senderIdentifier, String senderRole, String content) {
        OrderMessage message = OrderMessage.builder()
                .order(order)
                .senderIdentifier(senderIdentifier)
                .senderRole(senderRole)
                .content(content)
                .build();

        message = orderMessageRepository.save(message);
        OrderMessageDto dto = mapToDto(message);

        // Broadcast to the guest-facing order topic
        messagingTemplate.convertAndSend("/topic/order/" + order.getId(), dto);

        // Broadcast to the staff-facing property conversation-list topic
        messagingTemplate.convertAndSend("/topic/property/" + order.getPropertyId() + "/order-messages", dto);

        // SSE event for the live unread badge on the staff sidebar
        if ("GUEST".equals(senderRole)) {
            orderSseService.sendPropertyEvent(order.getPropertyId(), "new-order-message", dto);
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<OrderMessageDto> getMessagesForGuest(Long orderId, String guestSessionId) {
        Order order = findOrderOwnedBySession(orderId, guestSessionId);
        return getMessages(order.getId());
    }

    @Transactional(readOnly = true)
    public List<OrderMessageDto> getMessagesForStaff(Long orderId, String staffEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found", HttpStatus.NOT_FOUND));
        assertKitchenStaffAccess(staffEmail, order.getPropertyId());
        return getMessages(order.getId());
    }

    private List<OrderMessageDto> getMessages(Long orderId) {
        return orderMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiveOrderConversationDto> getConversationsForProperty(Long propertyId, String staffEmail) {
        assertKitchenStaffAccess(staffEmail, propertyId);

        List<Long> orderIds = orderMessageRepository.findOrderIdsWithMessagesByPropertyId(propertyId);

        return orderIds.stream().map(orderId -> {
            Order order = orderRepository.findById(orderId).orElseThrow();
            List<OrderMessage> messages = orderMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
            OrderMessage latestMessage = messages.get(messages.size() - 1);

            return ActiveOrderConversationDto.builder()
                    .orderId(order.getId())
                    .guestName(order.getGuestName())
                    .location(order.getLocation())
                    .orderStatus(order.getStatus() != null ? order.getStatus().name() : null)
                    .totalAmount(order.getTotalAmount())
                    .itemsSummary(buildItemsSummary(order))
                    .latestMessageContent(latestMessage.getContent())
                    .latestMessageAt(latestMessage.getCreatedAt())
                    .latestMessageSenderRole(latestMessage.getSenderRole())
                    .build();
        }).collect(Collectors.toList());
    }

    private String buildItemsSummary(Order order) {
        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return "";
        }

        List<String> names = items.stream()
                .map(item -> item.getQuantity() + "x " + (item.getMenuItem() != null ? item.getMenuItem().getName() : "Item"))
                .collect(Collectors.toList());

        if (names.size() <= 3) {
            return String.join(", ", names);
        }

        int remaining = names.size() - 3;
        return String.join(", ", names.subList(0, 3)) + " and " + remaining + " more";
    }

    private Order findOrderOwnedBySession(Long orderId, String guestSessionId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || guestSessionId == null || guestSessionId.isBlank()
                || order.getGuestSessionId() == null
                || !order.getGuestSessionId().equals(guestSessionId)) {
            throw new CustomException("Order not found", HttpStatus.NOT_FOUND);
        }
        return order;
    }

    /**
     * Mirror image of the booking-message staffRole gate: only staff whose staffRole is
     * "Kitchen Staff" or "Staff Admin" may access order conversations, and (for STAFF-role
     * accounts, which are scoped to a single property) only for their own property.
     * OWNER/ADMIN platform roles bypass the property scoping.
     */
    private User assertKitchenStaffAccess(String email, Long propertyId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (user.getRole() == UserRole.STAFF) {
            if (user.getStaffRole() == null || !KITCHEN_STAFF_ROLES.contains(user.getStaffRole())) {
                throw new CustomException("This inbox is not available for your staff role", HttpStatus.FORBIDDEN);
            }
            if (propertyId != null && !propertyId.equals(user.getPropertyId())) {
                throw new CustomException("You do not have access to this property's order messages", HttpStatus.FORBIDDEN);
            }
        }

        return user;
    }

    private OrderMessageDto mapToDto(OrderMessage message) {
        return OrderMessageDto.builder()
                .id(message.getId())
                .orderId(message.getOrder().getId())
                .senderIdentifier(message.getSenderIdentifier())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
