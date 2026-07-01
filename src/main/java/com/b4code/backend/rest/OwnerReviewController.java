package com.b4code.backend.rest;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.ReviewRepository;
import com.b4code.backend.dto.ReviewDTO.PropertyReviewsSummary;
import com.b4code.backend.dto.ReviewDTO.ReviewResponse;
import com.b4code.backend.models.Review;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner/reviews")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Owner — Reviews", description = "Owner-scoped review management")
public class OwnerReviewController {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;

    /**
     * GET /api/owner/reviews/property/{propertyId}?page=0&size=10
     * Returns a summary including averages and a paginated list of reviews.
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<PropertyReviewsSummary> getPropertyReviews(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Owner: fetching reviews for property {}, page={}, size={}", propertyId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId, pageable);

        Double avgRating = reviewRepository.calculateAverageRating(propertyId);
        Long totalReviews = reviewRepository.countByPropertyId(propertyId);

        String propertyName = propertyRepository.findById(propertyId)
                .map(p -> p.getName())
                .orElse(null);

        List<Review> pageContent = reviewPage.getContent();

        double avgCleanliness = average(pageContent.stream()
                .map(Review::getCleanlinessRating).collect(Collectors.toList()));
        double avgComfort = average(pageContent.stream()
                .map(Review::getComfortRating).collect(Collectors.toList()));
        double avgService = average(pageContent.stream()
                .map(Review::getServiceRating).collect(Collectors.toList()));
        double avgDining = average(pageContent.stream()
                .map(Review::getDiningRating).collect(Collectors.toList()));
        double avgLocation = average(pageContent.stream()
                .map(Review::getLocationRating).collect(Collectors.toList()));
        double avgValue = average(pageContent.stream()
                .map(Review::getValueRating).collect(Collectors.toList()));

        List<ReviewResponse> recentReviews = pageContent.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PropertyReviewsSummary summary = PropertyReviewsSummary.builder()
                .propertyId(propertyId)
                .propertyName(propertyName)
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .avgCleanliness(avgCleanliness)
                .avgComfort(avgComfort)
                .avgService(avgService)
                .avgDining(avgDining)
                .avgLocation(avgLocation)
                .avgValue(avgValue)
                .recentReviews(recentReviews)
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/owner/reviews/all?page=0&size=10
     * Returns all reviews paginated, sorted by createdAt desc.
     */
    @GetMapping("/all")
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Owner: fetching all reviews, page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        Page<ReviewResponse> responsePage = reviewPage.map(this::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private ReviewResponse toResponse(Review review) {
        List<String> photoUrls;
        if (review.getPhotoUrls() != null && !review.getPhotoUrls().isBlank()) {
            photoUrls = Arrays.asList(review.getPhotoUrls().split(","));
        } else {
            photoUrls = Collections.emptyList();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking() != null ? review.getBooking().getId() : null)
                .propertyId(review.getProperty() != null ? review.getProperty().getId() : null)
                .guestId(review.getGuest() != null ? review.getGuest().getId() : null)
                .overallRating(review.getOverallRating())
                .cleanlinessRating(review.getCleanlinessRating())
                .comfortRating(review.getComfortRating())
                .serviceRating(review.getServiceRating())
                .diningRating(review.getDiningRating())
                .locationRating(review.getLocationRating())
                .valueRating(review.getValueRating())
                .comment(review.getComment())
                .photoUrls(photoUrls)
                .createdAt(review.getCreatedAt())
                .build();
    }

    private double average(List<Integer> values) {
        OptionalDouble avg = values.stream()
                .filter(v -> v != null)
                .mapToInt(Integer::intValue)
                .average();
        return avg.isPresent() ? avg.getAsDouble() : 0.0;
    }
}
