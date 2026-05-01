package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.SeasonalPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonalPricingRepository extends JpaRepository<SeasonalPricing, Long> {

    List<SeasonalPricing> findByPropertyIdOrderByStartDateAsc(Long propertyId);
}
