package com.b4code.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

public class SearchDTO {

    // ─── Pagination Wrapper ──────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginatedResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    // ─── Search Result (card data) ───────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertySearchResult {
        private Long id;
        private String title;
        private String location;
        private String district;
        private String propertyType;
        private BigDecimal pricePerNight;
        private BigDecimal highestPricePerNight;
        private Integer maxGuests;
        private Integer baseGuests;
        private BigDecimal extraGuestFee;
        private Double rating;
        private Integer reviewCount;
        private String badge;
        private String imageSrc;
        private List<String> amenities;
        private Double lat;
        private Double lng;
        private Integer matchingRoomTypesCount;
    }

    // ─── Property Detail ─────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyDetailResult {
        private Long id;
        private String title;
        private String location;
        private String fullAddress;
        private String propertyType;
        private Boolean freeCancellation;
        private BigDecimal pricePerNight;
        private Double rating;
        private Integer reviewCount;
        private String badge;
        private String imageSrc;
        private List<String> galleryImages;

        private String hostName;
        private String hostBio;
        private Integer hostYears;
        private Boolean hostSuperhost;

        private String description;
        private List<AmenityDTO> amenities;

        private List<ReviewBreakdownDTO> reviewBreakdown;
        private List<ReviewDTO> reviews;

        private List<RoomDTO> roomTypes;

        private Double lat;
        private Double lng;

        private String checkInTime;
        private String checkOutTime;
        private String cancellationPolicy;
        private String childPolicy;
        private String houseRules;
    }

    // ─── Filter Options (served to frontend) ─────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterOptionsResponse {
        private List<PropertyTypeOption> propertyTypes;
        private List<String> amenities;
        private List<RatingOption> ratingOptions;
        private PriceRangeOption priceRange;
        private List<SortOption> sortOptions;
        private List<String> locations;
        private List<LocationSuggestionDTO> locationSuggestions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationSuggestionDTO {
        private String name;
        private String type; // "district" or "property"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyTypeOption {
        private String value;
        private String label;
        private String icon; // Lucide icon name
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingOption {
        private String label;
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceRangeOption {
        private BigDecimal min;
        private BigDecimal max;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SortOption {
        private String value;
        private String label;
    }

    // ─── Sub DTOs ────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AmenityDTO {
        private String icon;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewBreakdownDTO {
        private String label;
        private Double score;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDTO {
        private String id;
        private String author;
        private String avatarInitials;
        private String avatarColor;
        private String date;
        private String text;
        private Integer rating;

        private Integer cleanlinessRating;
        private Integer comfortRating;
        private Integer serviceRating;
        private Integer diningRating;
        private Integer locationRating;
        private Integer valueRating;

        private List<String> photoUrls;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomDTO {
        private String id;
        private String name;
        private Integer maxGuests;
        private String bedType;
        private Integer sqft;
        private BigDecimal pricePerNight;
        private BigDecimal originalPrice;
        private String tag;
        private List<String> features;
        private String imageSrc;
        private Integer availableCount;
        private String roomNumbers;
    }
}
