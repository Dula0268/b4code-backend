package com.b4code.backend.dao;

import com.b4code.backend.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.b4code.backend.models.enums.OrderStatus;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId, Pageable pageable);
    Page<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId, Pageable pageable);
    Page<Order> findByGuestIdAndGuestSessionIdOrderByCreatedAtDesc(Long guestId, String guestSessionId, Pageable pageable);
    Page<Order> findByGuestSessionIdOrderByCreatedAtDesc(String guestSessionId, Pageable pageable);
    List<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    List<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId);
    List<Order> findByGuestSessionIdOrderByCreatedAtDesc(String guestSessionId);

    @Query("SELECT o FROM Order o WHERE o.propertyId = :propertyId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR o.createdAt < :endDate) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findStaffOrders(@Param("propertyId") Long propertyId, 
                                @Param("status") OrderStatus status, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                Pageable pageable);
}
