package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {

    List<RatePlan> findByPropertyIdOrderByRoomTypeAsc(Long propertyId);

    List<RatePlan> findByPropertyIdAndStatus(Long propertyId, String status);
}
