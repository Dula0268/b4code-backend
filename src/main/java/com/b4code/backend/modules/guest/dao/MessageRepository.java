package com.b4code.backend.modules.guest.dao;

import com.b4code.backend.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByBookingIdOrderBySentAtAsc(Long bookingId);

    List<Message> findByBookingIdAndIsReadFalse(Long bookingId);
}
