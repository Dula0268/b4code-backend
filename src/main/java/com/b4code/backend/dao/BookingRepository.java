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

    @Query("""
        SELECT b FROM Booking b
        WHERE b.property.ownerId = :ownerId
          AND (:status IS NULL OR b.status = :status)
          AND (:search IS NULL OR :search = ''
               OR LOWER(b.guestName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(b.guestEmail) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(b.confirmationCode) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findByOwnerWithFilters(
            @Param("ownerId") Long ownerId,
            @Param("status") Booking.BookingStatus status,
            @Param("search") String search);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.id = :id AND b.property.ownerId = :ownerId
        """)
    java.util.Optional<Booking> findByIdAndPropertyOwnerId(
            @Param("id") Long id,
            @Param("ownerId") Long ownerId);

    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.property
        WHERE b.property.ownerId = :ownerId
          AND b.status <> 'CANCELLED'
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findRecentByOwner(@Param("ownerId") Long ownerId,
                                    org.springframework.data.domain.Pageable pageable);

    @Query("""
        SELECT SUM(b.totalAmount) FROM Booking b
        WHERE b.property.ownerId = :ownerId
          AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
        """)
    java.math.BigDecimal sumRevenueByOwner(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.property.ownerId = :ownerId
          AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
        """)
    long countActiveByOwner(@Param("ownerId") Long ownerId);
}
