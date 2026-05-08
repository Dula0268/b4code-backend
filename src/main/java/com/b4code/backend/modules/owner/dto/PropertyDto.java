package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTOs for Property module — covers listing, create, update, detail responses
 */
public class PropertyDto {

    @Data
    public static class PropertyRequest {
        private String name;
        private String description;
        private String address;
        private String city;
        private String postalCode;
        private String propertyType;
        private String contact;
        private String email;
        private String checkInTime;
        private String checkOutTime;
        private String rules;
        private Map<String, Boolean> amenities; // wifi: true, pool: false, etc.
    }

    @Data
    public static class PropertyResponse {
        private Long id;
        private String name;
        private String description;
        private String address;
        private String city;
        private String postalCode;
        private String propertyType;
        private String yearBuilt;
        private String contact;
        private String email;
        private String checkInTime;
        private String checkOutTime;
        private String rules;
        private String amenities;
        private String imageUrl;
        private BigDecimal rate;
        private Double rating;
        private Integer reviewCount;
        private String status;
        private Integer roomCount;
        // Quick stats for detail view
        private Double occupancyRate;
        private String managerName;
    }

    @Data
    public static class PropertyListItem {
        private Long id;
        private String name;
        private String address;
        private String image;
        private String rate;
        private Double rating;
        private Integer reviews;
        private String status;
        private Boolean statusOn;
    }

    @Data
    public static class PropertyListResponse {
        private List<PropertyListItem> properties;
        private int currentPage;
        private int totalPages;
        private long totalItems;
    }

    @Data
    public static class MediaResponse {
        private Long id;
        private String url;
        private String fileName;
        private String mediaType;
        private Boolean isPrimary;
    }
}
