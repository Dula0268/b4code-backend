package com.b4code.backend.dao;

import com.b4code.backend.models.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {
    List<RatePlan> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    Optional<RatePlan> findByIdAndPropertyId(Long id, Long propertyId);
}
