package com.b4code.backend.repository;

import com.b4code.backend.models.OrderMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderMessageRepository extends JpaRepository<OrderMessage, Long> {

    List<OrderMessage> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    @Query("SELECT DISTINCT m.order.id FROM OrderMessage m WHERE m.order.propertyId = :propertyId")
    List<Long> findOrderIdsWithMessagesByPropertyId(@Param("propertyId") Long propertyId);
}
