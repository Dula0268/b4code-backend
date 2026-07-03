package com.b4code.backend.service.impl;

import com.b4code.backend.dao.DiscountRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RatePlanRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.SeasonalPricingRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.DiscountDto;
import com.b4code.backend.dto.owner.DiscountRequest;
import com.b4code.backend.dto.owner.RateOverviewDto;
import com.b4code.backend.dto.owner.RatePlanDto;
import com.b4code.backend.dto.owner.RatePlanRequest;
import com.b4code.backend.dto.owner.RoomPriceDto;
import com.b4code.backend.dto.owner.SeasonalPricingDto;
import com.b4code.backend.dto.owner.SeasonalPricingRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Discount;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.RatePlan;
import com.b4code.backend.models.SeasonalPricing;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerRateServiceImpl implements OwnerRateService {

    private final RatePlanRepository ratePlanRepository;
    private final DiscountRepository discountRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final SeasonalPricingRepository seasonalPricingRepository;

    @Override
    @Transactional(readOnly = true)
    public RateOverviewDto getRateOverview(String ownerEmail, Long propertyId) {
        // Validate owner exists — no strict ownerId check (matches OwnerPropertyServiceImpl pattern)
        resolveOwner(ownerEmail);
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        List<RoomPriceDto> rooms = roomRepository.findByPropertyId(propertyId).stream()
                .map(r -> RoomPriceDto.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .pricePerNight(r.getPricePerNight() != null ? r.getPricePerNight().toPlainString() : "0")
                        .status(r.getStatus() != null ? r.getStatus().name() : "AVAILABLE")
                        .build())
                .toList();

        List<RatePlanDto> plans = ratePlanRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId)
                .stream().map(RatePlanDto::fromEntity).toList();
        List<DiscountDto> discounts = discountRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId)
                .stream().map(DiscountDto::fromEntity).toList();

        return RateOverviewDto.builder()
                .propertyId(propertyId)
                .rooms(rooms)
                .ratePlans(plans)
                .discounts(discounts)
                .seasonalPricing(Collections.emptyList())
                .build();
    }

    @Override
    @Transactional
    public RatePlanDto createRatePlan(String ownerEmail, RatePlanRequest request) {
        Property property = resolveOwnedProperty(ownerEmail, request.getPropertyId());
        RatePlan plan = RatePlan.builder()
                .property(property)
                .name(request.getName())
                .type(request.getType())
                .basePrice(request.getBasePrice())
                .minNights(request.getMinNights() != null ? request.getMinNights() : 1)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return RatePlanDto.fromEntity(ratePlanRepository.save(plan));
    }

    @Override
    @Transactional
    public RatePlanDto updateRatePlan(String ownerEmail, Long id, RatePlanRequest request) {
        User owner = resolveOwner(ownerEmail);
        RatePlan plan = ratePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException("Rate plan not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(plan.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        if (request.getName() != null)      plan.setName(request.getName());
        if (request.getType() != null)      plan.setType(request.getType());
        if (request.getBasePrice() != null) plan.setBasePrice(request.getBasePrice());
        if (request.getMinNights() != null) plan.setMinNights(request.getMinNights());
        if (request.getStartDate() != null) plan.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)   plan.setEndDate(request.getEndDate());
        if (request.getIsActive() != null)  plan.setIsActive(request.getIsActive());
        return RatePlanDto.fromEntity(ratePlanRepository.save(plan));
    }

    @Override
    @Transactional
    public void deleteRatePlan(String ownerEmail, Long id) {
        User owner = resolveOwner(ownerEmail);
        RatePlan plan = ratePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException("Rate plan not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(plan.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        ratePlanRepository.delete(plan);
    }

    @Override
    @Transactional
    public DiscountDto createDiscount(String ownerEmail, DiscountRequest request) {
        Property property = resolveOwnedProperty(ownerEmail, request.getPropertyId());
        Discount d = Discount.builder()
                .property(property)
                .name(request.getName())
                .type(request.getType())
                .percentage(request.getPercentage())
                .minNights(request.getMinNights())
                .daysInAdvance(request.getDaysInAdvance())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return DiscountDto.fromEntity(discountRepository.save(d));
    }

    @Override
    @Transactional
    public DiscountDto updateDiscount(String ownerEmail, Long id, DiscountRequest request) {
        User owner = resolveOwner(ownerEmail);
        Discount d = discountRepository.findById(id)
                .orElseThrow(() -> new CustomException("Discount not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(d.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        if (request.getName() != null)         d.setName(request.getName());
        if (request.getType() != null)         d.setType(request.getType());
        if (request.getPercentage() != null)   d.setPercentage(request.getPercentage());
        if (request.getMinNights() != null)    d.setMinNights(request.getMinNights());
        if (request.getDaysInAdvance() != null) d.setDaysInAdvance(request.getDaysInAdvance());
        if (request.getStartDate() != null)    d.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)      d.setEndDate(request.getEndDate());
        if (request.getIsActive() != null)     d.setIsActive(request.getIsActive());
        return DiscountDto.fromEntity(discountRepository.save(d));
    }

    @Override
    @Transactional
    public void deleteDiscount(String ownerEmail, Long id) {
        User owner = resolveOwner(ownerEmail);
        Discount d = discountRepository.findById(id)
                .orElseThrow(() -> new CustomException("Discount not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(d.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        discountRepository.delete(d);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeasonalPricingDto> getSeasonalPricing(String ownerEmail, Long propertyId) {
        resolveOwner(ownerEmail);
        return seasonalPricingRepository.findByPropertyIdOrderByStartDateAsc(propertyId)
                .stream().map(SeasonalPricingDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public SeasonalPricingDto createSeasonalPricing(String ownerEmail, SeasonalPricingRequest request) {
        resolveOwner(ownerEmail);
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        SeasonalPricing s = SeasonalPricing.builder()
                .property(property)
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .percentageAdjustment(request.getPercentageAdjustment())
                .build();
        try {
            return SeasonalPricingDto.fromEntity(seasonalPricingRepository.save(s));
        } catch (Exception e) {
            log.error("Failed to save seasonal pricing — run the DB migration first: {}", e.getMessage());
            throw new CustomException(
                "Seasonal pricing table not found. Run: CREATE TABLE IF NOT EXISTS owner.seasonal_pricing (...)",
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteSeasonalPricing(String ownerEmail, Long id) {
        resolveOwner(ownerEmail);
        SeasonalPricing s = seasonalPricingRepository.findById(id)
                .orElseThrow(() -> new CustomException("Seasonal pricing not found", HttpStatus.NOT_FOUND));
        seasonalPricingRepository.delete(s);
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }

    private Property resolveOwnedProperty(String ownerEmail, Long propertyId) {
        User owner = resolveOwner(ownerEmail);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        return property;
    }
}
