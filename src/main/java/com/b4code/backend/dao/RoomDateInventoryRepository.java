package com.b4code.backend.dao;

import com.b4code.backend.models.RoomDateInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomDateInventoryRepository extends JpaRepository<RoomDateInventory, Long> {

    Optional<RoomDateInventory> findByRoomIdAndDate(Long roomId, LocalDate date);

    List<RoomDateInventory> findByRoomIdAndDateBetween(Long roomId, LocalDate from, LocalDate to);

    @Query("""
            SELECT rdi FROM RoomDateInventory rdi
            WHERE rdi.room.property.id = :propertyId
              AND rdi.date >= :from
              AND rdi.date <= :to
            ORDER BY rdi.room.id, rdi.date
            """)
    List<RoomDateInventory> findByPropertyAndDateRange(
            @Param("propertyId") Long propertyId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(rdi.bookedQuantity), 0) FROM RoomDateInventory rdi
            WHERE rdi.room.id = :roomId
              AND rdi.date >= :from
              AND rdi.date <= :to
            """)
    int sumBookedForRange(
            @Param("roomId") Long roomId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
