package com.b4code.backend.dao;

import com.b4code.backend.models.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {
}
