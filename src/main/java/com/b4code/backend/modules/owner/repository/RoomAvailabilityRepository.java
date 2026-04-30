package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {

    List<RoomAvailability> findByPropertyIdAndDateBetweenOrderByDateAsc(Long propertyId, LocalDate start, LocalDate end);

    List<RoomAvailability> findByRoomIdAndDateBetweenOrderByDateAsc(Long roomId, LocalDate start, LocalDate end);

    RoomAvailability findByRoomIdAndDate(Long roomId, LocalDate date);

    void deleteByRoomIdAndDateBetween(Long roomId, LocalDate start, LocalDate end);
}
