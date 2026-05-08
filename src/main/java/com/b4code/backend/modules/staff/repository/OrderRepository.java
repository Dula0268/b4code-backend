package com.b4code.backend.modules.staff.repository;

import com.b4code.backend.modules.staff.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
}
