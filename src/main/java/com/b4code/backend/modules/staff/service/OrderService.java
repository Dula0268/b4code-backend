package com.b4code.backend.modules.staff.service;

import com.b4code.backend.modules.staff.dto.OrderDTO;
import com.b4code.backend.modules.staff.entity.Order;
import com.b4code.backend.modules.staff.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setPropertyId(orderDTO.getPropertyId());
        order.setGuestId(orderDTO.getGuestId());
        order.setRoomNumber(orderDTO.getRoomNumber());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setStatus("NEW");
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDTO(order);
    }

    public List<OrderDTO> getOrdersByPropertyId(Long propertyId) {
        return orderRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByGuestId(Long guestId) {
        return orderRepository.findByGuestIdOrderByCreatedAtDesc(guestId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByPropertyIdAndStatus(Long propertyId, String status) {
        return orderRepository.findByPropertyIdAndStatus(propertyId, status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (orderDTO.getRoomNumber() != null) {
            order.setRoomNumber(orderDTO.getRoomNumber());
        }
        if (orderDTO.getTotalAmount() != null) {
            order.setTotalAmount(orderDTO.getTotalAmount());
        }
        if (orderDTO.getStatus() != null) {
            order.setStatus(orderDTO.getStatus());
        }
        
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    private OrderDTO convertToDTO(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getPropertyId(),
                order.getGuestId(),
                order.getRoomNumber(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
