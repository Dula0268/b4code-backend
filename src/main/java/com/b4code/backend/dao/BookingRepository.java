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

    int countByGuestEmailAndPaymentMethodAndStatusIn(String guestEmail, Booking.PaymentMethod paymentMethod, List<Booking.BookingStatus> statuses);

    @Query("""
        SELECT COALESCE(SUM(b.roomQuantity), 0) FROM Booking b
        WHERE b.roomType.id = :roomId
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
          AND b.roomType.id = :roomId
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
    List<Booking> findByPropertyId(Long propertyId);
    List<Booking> findByPropertyIdAndStatus(Long propertyId, Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.checkIn = :checkIn AND b.status = :status")
    List<Booking> findByCheckInAndStatus(@Param("checkIn") LocalDate checkIn, @Param("status") Booking.BookingStatus status);

    Optional<Booking> findByConfirmationCode(String confirmationCode);

    /** Used by check-in (to stop double-assigning an occupied room) and by the
     * guest-facing room-status lookup (to know if a scanned room QR is checked in). */
    Optional<Booking> findByPropertyIdAndRoomNumberIgnoreCaseAndStatus(
        Long propertyId, String roomNumber, Booking.BookingStatus status);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.property.id = :propertyId
          AND b.roomType.id = :roomId
          AND b.status = 'CHECKED_IN'
    """)
    Optional<Booking> findActiveBookingByRoom(
        @Param("propertyId") Long propertyId,
        @Param("roomId") Long roomId
    );

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
        WHERE b.property.ownerId = :ownerId
          AND b.status <> 'CANCELLED'
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findRecentByOwner(@Param("ownerId") Long ownerId,
                                    org.springframework.data.domain.Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b
        WHERE b.property.ownerId = :ownerId
          AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
        """)
    java.math.BigDecimal sumRevenueByOwner(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b
        WHERE b.property.ownerId = :ownerId
          AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
          AND b.checkIn >= :from AND b.checkIn < :to
        """)
    java.math.BigDecimal sumRevenueByOwnerAndMonth(@Param("ownerId") Long ownerId,
                                                     @Param("from") java.time.LocalDate from,
                                                     @Param("to") java.time.LocalDate to);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.property.ownerId = :ownerId
          AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
        """)
    long countActiveByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.property.ownerId = :ownerId")
    long countAllByOwner(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.property.ownerId = :ownerId
          AND b.checkIn = :date
          AND b.status IN ('CONFIRMED', 'CHECKED_IN')
        """)
    long countCheckInsByOwnerAndDate(@Param("ownerId") Long ownerId, @Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')")
    java.math.BigDecimal sumPlatformGrossRevenue();

    @Query(value = """
        SELECT TO_CHAR(b.created_at, 'Mon') AS month,
               SUM(b.total_amount) AS total
        FROM guest.bookings b
        WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
        GROUP BY TO_CHAR(b.created_at, 'Mon'),
                 EXTRACT(MONTH FROM b.created_at)
        ORDER BY EXTRACT(MONTH FROM b.created_at)
        """, nativeQuery = true)
    java.util.List<Object[]> getMonthlyBookingRevenueTrend();

    @Query(value = """
        SELECT TO_CHAR(b.created_at, 'HH24:00') AS timeLabel,
               SUM(b.total_amount) AS total
        FROM guest.bookings b
        WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
          AND CAST(b.created_at AS DATE) = CURRENT_DATE
        GROUP BY TO_CHAR(b.created_at, 'HH24:00')
        ORDER BY TO_CHAR(b.created_at, 'HH24:00')
        """, nativeQuery = true)
    java.util.List<Object[]> getTodayBookingRevenueTrend();

    @Query(value = """
        SELECT TO_CHAR(b.created_at, 'Dy') AS timeLabel,
               SUM(b.total_amount) AS total
        FROM guest.bookings b
        WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
          AND b.created_at >= CURRENT_DATE - INTERVAL '6 days'
        GROUP BY TO_CHAR(b.created_at, 'Dy'),
                 CAST(b.created_at AS DATE)
        ORDER BY CAST(b.created_at AS DATE)
        """, nativeQuery = true)
    java.util.List<Object[]> getWeeklyBookingRevenueTrend();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN')")
    long countActiveBookings();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'")
    long countCancelledBookings();

    @Query(value = "SELECT COALESCE(AVG(b.check_in - CAST(b.created_at AS DATE)), 0) FROM guest.bookings b", nativeQuery = true)
    Double getAverageLeadTime();
}
