package com.b4code.backend.repository;

import com.b4code.backend.models.BookingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingMessageRepository extends JpaRepository<BookingMessage, Long> {

    List<BookingMessage> findByBookingIdOrderByCreatedAtAsc(Long bookingId);

    @Query("SELECT DISTINCT m.booking.id FROM BookingMessage m WHERE m.booking.property.id = :propertyId")
    List<Long> findBookingIdsWithMessagesByPropertyId(@Param("propertyId") Long propertyId);
}
