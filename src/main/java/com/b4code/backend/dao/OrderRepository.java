package com.b4code.backend.dao;

import com.b4code.backend.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId, Pageable pageable);
    Page<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId, Pageable pageable);
    List<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    List<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId);
}
