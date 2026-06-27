package com.b4code.backend.dao;

import com.b4code.backend.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByBookingIdOrderBySentAtAsc(Long bookingId);

    List<Message> findByBookingIdAndIsReadFalse(Long bookingId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT m FROM Message m
            WHERE m.booking.property.ownerId = :ownerId
              AND m.id IN (
                  SELECT MAX(m2.id) FROM Message m2
                  WHERE m2.booking.property.ownerId = :ownerId
                  GROUP BY m2.booking.id
              )
            ORDER BY m.sentAt DESC
            """)
    List<Message> findLatestMessagePerBookingByOwner(
            @org.springframework.data.repository.query.Param("ownerId") Long ownerId);

    long countByBookingIdAndIsReadFalseAndSenderType(
            Long bookingId, Message.SenderType senderType);
}
