package com.b4code.backend.rest;

import com.b4code.backend.dto.*;
import com.b4code.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest/reviews")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * POST /api/v1/reviews
     * Create a new review (guest only, verified stay).
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/reviews/property/{propertyId}
     * Get paginated reviews for a property.
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<PropertyReviewsSummary> getPropertyReviews(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(reviewService.getPropertyReviews(propertyId, page, size));
    }

    /**
     * POST /api/v1/reviews/{reviewId}/owner-response
     * Owner responds to a review.
     */
    @PostMapping("/{reviewId}/owner-response")
    public ResponseEntity<ReviewResponse> addOwnerResponse(
            @PathVariable Long reviewId,
            @Valid @RequestBody OwnerResponseRequest request) {

        ReviewResponse response = reviewService.addOwnerResponse(reviewId, request);
        return ResponseEntity.ok(response);
    }
}




