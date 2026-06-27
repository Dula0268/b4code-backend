package com.b4code.backend.dao;

import com.b4code.backend.models.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    @Query("""
            SELECT a FROM Availability a
            WHERE a.room.property.id = :propertyId
              AND a.date >= :from
              AND a.date <= :to
            ORDER BY a.room.id, a.date
            """)
    List<Availability> findByPropertyAndDateRange(
            @Param("propertyId") Long propertyId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    Optional<Availability> findByRoomIdAndDate(Long roomId, LocalDate date);

    List<Availability> findByRoomIdAndDateBetween(Long roomId, LocalDate from, LocalDate to);
}
