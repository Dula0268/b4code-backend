package com.b4code.backend.modules.guest.dao;

import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByConfirmationNumber(String confirmationNumber);

    List<Booking> findByGuestEmail(String guestEmail);

    /**
     * Check if a room is already booked for the requested date range.
     * Overlapping condition: existing check-in < new check-out AND existing check-out > new check-in
     */
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status NOT IN ('CANCELLED')
          AND b.checkIn  < :checkOut
          AND b.checkOut > :checkIn
    """)
    boolean existsOverlappingBooking(
        @Param("roomId")   Long roomId,
        @Param("checkIn")  LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.id <> :bookingId
          AND b.room.id = :roomId
          AND b.status NOT IN ('CANCELLED')
          AND b.checkIn  < :checkOut
          AND b.checkOut > :checkIn
    """)
    boolean existsOverlappingBookingExcludingId(
        @Param("bookingId") Long bookingId,
        @Param("roomId") Long roomId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    List<Booking> findByRoomPropertyIdAndStatus(Long propertyId, BookingStatus status);
}
