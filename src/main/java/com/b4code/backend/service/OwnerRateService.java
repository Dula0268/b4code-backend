package com.b4code.backend.service;

import com.b4code.backend.dto.owner.DiscountDto;
import com.b4code.backend.dto.owner.DiscountRequest;
import com.b4code.backend.dto.owner.RateOverviewDto;
import com.b4code.backend.dto.owner.RatePlanDto;
import com.b4code.backend.dto.owner.RatePlanRequest;

public interface OwnerRateService {
    RateOverviewDto getRateOverview(String ownerEmail, Long propertyId);
    RatePlanDto createRatePlan(String ownerEmail, RatePlanRequest request);
    RatePlanDto updateRatePlan(String ownerEmail, Long id, RatePlanRequest request);
    void deleteRatePlan(String ownerEmail, Long id);
    DiscountDto createDiscount(String ownerEmail, DiscountRequest request);
    DiscountDto updateDiscount(String ownerEmail, Long id, DiscountRequest request);
    void deleteDiscount(String ownerEmail, Long id);
}
