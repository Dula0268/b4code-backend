package com.b4code.backend.modules.staff.repository;

import com.b4code.backend.modules.staff.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    org.springframework.data.domain.Page<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId, org.springframework.data.domain.Pageable pageable);
    List<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    List<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId);
}
