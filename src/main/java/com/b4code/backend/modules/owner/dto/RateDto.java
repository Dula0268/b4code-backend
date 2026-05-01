package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs for Rate module — rate plans, discounts, seasonal pricing
 */
public class RateDto {

    @Data
    public static class RatePlanRequest {
        private Long propertyId;
        private String roomType;
        private BigDecimal basePrice;
        private String weekendPercentage;
        private String status;
    }

    @Data
    public static class RatePlanResponse {
        private Long id;
        private String roomType;
        private BigDecimal basePrice;
        private String weekendPercentage;
        private String status;
    }

    @Data
    public static class DiscountRequest {
        private Long propertyId;
        private String name;
        private String description;
        private String percentage;
        private String discountType;
        private Boolean active;
    }

    @Data
    public static class DiscountResponse {
        private Long id;
        private String name;
        private String description;
        private String percentage;
        private String discountType;
        private Boolean active;
    }

    @Data
    public static class SeasonalPricingResponse {
        private Long id;
        private String name;
        private String dateRange;
        private String percentage;
        private Integer progress;
    }

    @Data
    public static class RateOverviewResponse {
        private BigDecimal averageNightlyRate;
        private Double occupancyForecast;
        private int activeDiscountCount;
        private List<RatePlanResponse> ratePlans;
        private List<DiscountResponse> discounts;
        private List<SeasonalPricingResponse> seasonalPricing;
        private WeekendMultiplier weekendMultiplier;
    }

    @Data
    public static class WeekendMultiplier {
        private Boolean fridaySaturday;
        private Integer fridaySaturdayPercent;
        private Boolean sundayNight;
        private Integer sundayPercent;
    }
}
