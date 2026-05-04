package com.b4code.backend.modules.guest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// ──────────────────────────────────────
// Search Request
// ──────────────────────────────────────
public class SearchDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        private String destination;        // city or property name
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer guests;
        private Integer rooms;

        // filters
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Double minRating;
        private Boolean freeCancel;
        private Boolean breakfastIncluded;
        private Boolean petFriendly;
    }

    // ──────────────────────────────────
    // Search Result Item
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertySearchResult {
        private Long propertyId;
        private String name;
        private String city;
        private String address;
        private Double latitude;
        private Double longitude;
        private Double averageRating;
        private Integer reviewCount;
        private BigDecimal lowestPricePerNight;  // cheapest available room
        private List<RoomSummary> availableRooms;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomSummary {
        private Long roomId;
        private String name;
        private String roomType;
        private Integer maxOccupancy;
        private BigDecimal pricePerNight;
        private String amenities;
    }
}