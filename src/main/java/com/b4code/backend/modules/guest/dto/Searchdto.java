package com.b4code.backend.modules.guest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SearchDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        private String destination;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer guests;
        private Integer rooms;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Double minRating;
        private List<String> propertyTypes;
        private List<String> amenities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertySearchResult {
        private Long id;
        private String title;
        private String location;
        private String propertyType;
        private BigDecimal pricePerNight;
        private Integer maxGuests;
        private Integer baseGuests;
        private BigDecimal extraGuestFee;
        private Double rating;
        private Integer reviewCount;
        private String badge;
        private String imageSrc;
    }

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
        
        private List<RoomDTO> rooms;
        
        private Double lat;
        private Double lng;
    }
    
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
        private String ownerReply;
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
    }
}