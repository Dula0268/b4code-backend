package com.b4code.backend.dao;

import com.b4code.backend.models.RoomBookingDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomBookingDailyRepository extends JpaRepository<RoomBookingDaily, Long> {

    Optional<RoomBookingDaily> findByRoomIdAndDate(Long roomId, LocalDate date);

    List<RoomBookingDaily> findByRoomIdAndDateBetween(Long roomId, LocalDate from, LocalDate to);

    @Query("""
            SELECT rbd FROM RoomBookingDaily rbd
            WHERE rbd.room.property.id = :propertyId
              AND rbd.date >= :from
              AND rbd.date <= :to
            ORDER BY rbd.room.id, rbd.date
            """)
    List<RoomBookingDaily> findByPropertyAndDateRange(
            @Param("propertyId") Long propertyId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(rbd.bookedQuantity), 0) FROM RoomBookingDaily rbd
            WHERE rbd.room.id = :roomId
              AND rbd.date >= :from
              AND rbd.date <= :to
            """)
    int sumBookedQuantityForRange(
            @Param("roomId") Long roomId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
