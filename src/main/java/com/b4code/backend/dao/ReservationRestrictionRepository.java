package com.b4code.backend.dao;

import com.b4code.backend.models.ReservationRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRestrictionRepository extends JpaRepository<ReservationRestriction, Long> {
    List<ReservationRestriction> findByPropertyIdOrderByStartDateDesc(Long propertyId);
    Optional<ReservationRestriction> findByIdAndPropertyId(Long id, Long propertyId);
}
