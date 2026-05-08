package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPropertyIdInOrderByCreatedAtDesc(List<Long> propertyIds);

    List<Reservation> findTop10ByPropertyIdInOrderByCreatedAtDesc(List<Long> propertyIds);

    long countByPropertyIdInAndStatusIn(List<Long> propertyIds, List<String> statuses);

    long countByPropertyIdInAndCheckInDate(List<Long> propertyIds, LocalDate checkInDate);

    long countByPropertyIdInAndCheckOutDate(List<Long> propertyIds, LocalDate checkOutDate);

    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.propertyId IN :propertyIds AND r.status <> 'CANCELLED'")
    BigDecimal sumTotalRevenueByPropertyIds(@Param("propertyIds") List<Long> propertyIds);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.propertyId IN :propertyIds AND r.checkInDate <= :date AND r.checkOutDate >= :date AND r.status IN ('CONFIRMED', 'CHECKED_IN')")
    long countOccupiedRoomsOnDate(@Param("propertyIds") List<Long> propertyIds, @Param("date") LocalDate date);

    @Query("SELECT r.checkInDate, COUNT(r) FROM Reservation r WHERE r.propertyId IN :propertyIds AND r.checkInDate BETWEEN :startDate AND :endDate AND r.status <> 'CANCELLED' GROUP BY r.checkInDate")
    List<Object[]> countReservationsByDateRange(@Param("propertyIds") List<Long> propertyIds,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Reservation> findByPropertyIdOrderByCheckInDateDesc(Long propertyId);
}
