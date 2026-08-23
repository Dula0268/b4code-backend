package com.b4code.backend.dao;

import com.b4code.backend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Payment p WHERE p.booking.property.ownerId = :ownerId ORDER BY p.createdAt DESC")
    List<Payment> findByPropertyOwnerId(@org.springframework.data.repository.query.Param("ownerId") Long ownerId);

    List<Payment> findAllByOrderByCreatedAtDesc();

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findFirstByBookingIdAndStatusOrderByCreatedAtDesc(Long bookingId, Payment.PaymentStatus status);

    Optional<Payment> findFirstByFoodOrderIdAndStatusOrderByCreatedAtDesc(Long foodOrderId, Payment.PaymentStatus status);

    List<Payment> findByFoodOrderIdOrderByCreatedAtDesc(Long foodOrderId);
}
