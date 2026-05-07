package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByPropertyIdOrderByCheckInDateDesc(Long propertyId);
}
