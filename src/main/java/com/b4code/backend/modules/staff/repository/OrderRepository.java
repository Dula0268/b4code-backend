package com.b4code.backend.modules.staff.repository;

import com.b4code.backend.modules.staff.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPropertyId(Long propertyId);
    List<Order> findByGuestId(Long guestId);
    List<Order> findByStatus(String status);
    List<Order> findByPropertyIdAndStatus(Long propertyId, String status);
    List<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId);
}
