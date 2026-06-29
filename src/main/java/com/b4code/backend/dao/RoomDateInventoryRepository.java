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

    @Query("SELECT ri FROM RoomDateInventory ri WHERE ri.room.id = :roomId AND ri.date >= :startDate AND ri.date < :endDate")
    List<RoomDateInventory> findByRoomIdAndDates(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
