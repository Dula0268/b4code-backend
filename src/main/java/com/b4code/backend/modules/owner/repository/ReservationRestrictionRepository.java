package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.ReservationRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRestrictionRepository extends JpaRepository<ReservationRestriction, Long> {

    List<ReservationRestriction> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
}
