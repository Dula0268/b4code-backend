package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.RateDto.*;
import com.b4code.backend.modules.owner.entity.*;
import com.b4code.backend.modules.owner.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateService {

    private final RatePlanRepository ratePlanRepository;
    private final DiscountRepository discountRepository;
    private final SeasonalPricingRepository seasonalRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd");

    public RateOverviewResponse getRateOverview(Long propertyId) {
        List<RatePlan> plans = ratePlanRepository.findByPropertyIdOrderByRoomTypeAsc(propertyId);
        List<Discount> discounts = discountRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
        List<SeasonalPricing> seasons = seasonalRepository.findByPropertyIdOrderByStartDateAsc(propertyId);

        BigDecimal avg = plans.isEmpty() ? BigDecimal.ZERO : plans.stream().map(RatePlan::getBasePrice).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(plans.size()), 2, RoundingMode.HALF_UP);
        int activeDiscounts = (int) discounts.stream().filter(d -> Boolean.TRUE.equals(d.getActive())).count();

        RateOverviewResponse resp = new RateOverviewResponse();
        resp.setAverageNightlyRate(avg);
        resp.setActiveDiscountCount(activeDiscounts);
        resp.setRatePlans(plans.stream().map(this::toRatePlanResponse).collect(Collectors.toList()));
        resp.setDiscounts(discounts.stream().map(this::toDiscountResponse).collect(Collectors.toList()));
        resp.setSeasonalPricing(seasons.stream().map(this::toSeasonalResponse).collect(Collectors.toList()));

        // Weekend multiplier from first plan
        if (!plans.isEmpty()) {
            RatePlan first = plans.get(0);
            WeekendMultiplier wm = new WeekendMultiplier();
            wm.setFridaySaturday(first.getWeekendFridaySaturday());
            wm.setFridaySaturdayPercent(first.getWeekendMultiplierPercent());
            wm.setSundayNight(first.getSundayNight());
            wm.setSundayPercent(first.getSundayMultiplierPercent());
            resp.setWeekendMultiplier(wm);
        }
        return resp;
    }

    @Transactional
    public RatePlanResponse createRatePlan(RatePlanRequest req) {
        RatePlan p = new RatePlan();
        p.setPropertyId(req.getPropertyId()); p.setRoomType(req.getRoomType());
        p.setBasePrice(req.getBasePrice()); p.setWeekendPercentage(req.getWeekendPercentage());
        p.setStatus(req.getStatus() != null ? req.getStatus() : "ACTIVE");
        return toRatePlanResponse(ratePlanRepository.save(p));
    }

    @Transactional
    public RatePlanResponse updateRatePlan(Long id, RatePlanRequest req) {
        RatePlan p = ratePlanRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (req.getRoomType() != null) p.setRoomType(req.getRoomType());
        if (req.getBasePrice() != null) p.setBasePrice(req.getBasePrice());
        if (req.getWeekendPercentage() != null) p.setWeekendPercentage(req.getWeekendPercentage());
        if (req.getStatus() != null) p.setStatus(req.getStatus());
        return toRatePlanResponse(ratePlanRepository.save(p));
    }

    @Transactional
    public void deleteRatePlan(Long id) { ratePlanRepository.deleteById(id); }

    @Transactional
    public DiscountResponse createDiscount(DiscountRequest req) {
        Discount d = new Discount();
        d.setPropertyId(req.getPropertyId()); d.setName(req.getName()); d.setDescription(req.getDescription());
        d.setPercentage(req.getPercentage()); d.setDiscountType(req.getDiscountType()); d.setActive(req.getActive());
        return toDiscountResponse(discountRepository.save(d));
    }

    @Transactional
    public DiscountResponse updateDiscount(Long id, DiscountRequest req) {
        Discount d = discountRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (req.getName() != null) d.setName(req.getName());
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        if (req.getPercentage() != null) d.setPercentage(req.getPercentage());
        if (req.getActive() != null) d.setActive(req.getActive());
        return toDiscountResponse(discountRepository.save(d));
    }

    @Transactional
    public void deleteDiscount(Long id) { discountRepository.deleteById(id); }

    private RatePlanResponse toRatePlanResponse(RatePlan p) {
        RatePlanResponse r = new RatePlanResponse();
        r.setId(p.getId()); r.setRoomType(p.getRoomType()); r.setBasePrice(p.getBasePrice());
        r.setWeekendPercentage(p.getWeekendPercentage()); r.setStatus(p.getStatus());
        return r;
    }

    private DiscountResponse toDiscountResponse(Discount d) {
        DiscountResponse r = new DiscountResponse();
        r.setId(d.getId()); r.setName(d.getName()); r.setDescription(d.getDescription());
        r.setPercentage(d.getPercentage()); r.setDiscountType(d.getDiscountType()); r.setActive(d.getActive());
        return r;
    }

    private SeasonalPricingResponse toSeasonalResponse(SeasonalPricing s) {
        SeasonalPricingResponse r = new SeasonalPricingResponse();
        r.setId(s.getId()); r.setName(s.getName()); r.setPercentage(s.getPercentage()); r.setProgress(s.getProgress());
        r.setDateRange(s.getStartDate().format(FMT) + " - " + s.getEndDate().format(FMT));
        return r;
    }
}
