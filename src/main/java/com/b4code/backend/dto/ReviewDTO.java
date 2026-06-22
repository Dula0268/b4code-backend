package com.b4code.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewDTO {

    // ──────────────────────────────────
    // Create Review Request
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateReviewRequest {

        @NotNull(message = "Booking ID is required")
        private Long bookingId;

        // Added for mock testing to specify the property when no real booking exists
        private Long propertyId;

        @NotNull(message = "Overall rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        private Integer overallRating;



        @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
        private String comment;

        private Integer cleanlinessRating;
        private Integer comfortRating;
        private Integer serviceRating;
        private Integer diningRating;
        private Integer locationRating;
        private Integer valueRating;

        private List<String> photoUrls;  // uploaded images
    }

    // ──────────────────────────────────
    // Review Response
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponse {
        private Long id;
        private Long bookingId;
        private Long propertyId;
        private Long guestId;
        private Integer overallRating;
        
        private Integer cleanlinessRating;
        private Integer comfortRating;
        private Integer serviceRating;
        private Integer diningRating;
        private Integer locationRating;
        private Integer valueRating;

        private String comment;
        private List<String> photoUrls;
        private LocalDateTime createdAt;
    }

    // ──────────────────────────────────
    // Property Reviews Summary
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyReviewsSummary {
        private Long propertyId;
        private String propertyName;
        private Double averageRating;
        private Long totalReviews;

        private Double avgCleanliness;
        private Double avgComfort;
        private Double avgService;
        private Double avgDining;
        private Double avgLocation;
        private Double avgValue;

        private List<ReviewResponse> recentReviews;
    }

}

