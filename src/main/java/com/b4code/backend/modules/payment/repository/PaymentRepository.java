package com.b4code.backend.modules.payment.repository;

import com.b4code.backend.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findAllByOrderByCreatedAtDesc();

    Optional<Payment> findByOrderId(String orderId);
}