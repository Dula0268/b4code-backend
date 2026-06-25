package com.b4code.backend.dao;

import com.b4code.backend.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT COALESCE(SUM(b.roomQuantity), 0) FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status <> 'CANCELLED'
          AND b.checkIn  < :checkOut
          AND b.checkOut > :checkIn
    """)
    int getBookedQuantityForDates(
        @Param("roomId")   Long roomId,
        @Param("checkIn")  LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    @Query("""
        SELECT COALESCE(SUM(b.roomQuantity), 0) FROM Booking b
        WHERE b.id <> :bookingId
          AND b.room.id = :roomId
          AND b.status <> 'CANCELLED'
          AND b.checkIn  < :checkOut
          AND b.checkOut > :checkIn
    """)
    int getBookedQuantityForDatesExcludingId(
        @Param("bookingId") Long bookingId,
        @Param("roomId") Long roomId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    List<Booking> findByGuestEmailOrderByCreatedAtDesc(String guestEmail);

    Optional<Booking> findByConfirmationCode(String confirmationCode);

    @Query(value = """
        SELECT b.room_id AS roomId,
               COALESCE(SUM(b.total_amount), 0) AS totalRevenue,
               COALESCE(SUM(b.check_out - b.check_in), 0) AS totalNights
        FROM guest.bookings b
        WHERE b.status IN ('COMPLETED', 'CHECKED_IN', 'CONFIRMED')
        GROUP BY b.room_id
    """, nativeQuery = true)
    List<Object[]> getRoomAggregatedMetrics();
}
