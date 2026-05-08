package com.hospitality.dao;

import com.hospitality.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByBookingIdOrderBySentAtAsc(Long bookingId);

    List<Message> findByBookingIdAndIsReadFalse(Long bookingId);
}