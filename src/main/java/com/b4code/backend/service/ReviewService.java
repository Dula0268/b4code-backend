package com.b4code.backend.service;

import com.b4code.backend.dto.ReviewDTO.*;
import com.b4code.backend.exceptions.ResourceNotFoundException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Review;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;

    /**
     * Create a review (guests can only review completed bookings).
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking not found: " + request.getBookingId()));

        // Business rule: booking must be completed
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("Can only review completed stays");
        }

        // Prevent duplicate reviews
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("Review already exists for this booking");
        }

        String photoUrlsStr = request.getPhotoUrls() != null
            ? String.join(",", request.getPhotoUrls())
            : null;

        Review review = Review.builder()
            .booking(booking)
            .property(booking.getRoom().getProperty())
            .guestName(booking.getGuestName())
            .overallRating(request.getOverallRating())
            .cleanlinessRating(request.getCleanlinessRating())
            .accuracyRating(request.getAccuracyRating())
            .communicationRating(request.getCommunicationRating())
            .locationRating(request.getLocationRating())
            .valueRating(request.getValueRating())
            .comment(request.getComment())
            .photoUrls(photoUrlsStr)
            .isVerifiedStay(true)
            .build();

        Review saved = reviewRepository.save(review);

        // Update property average rating
        updatePropertyRating(booking.getRoom().getProperty().getId());

        return mapToResponse(saved);
    }

    /**
     * Get reviews for a property (paginated).
     */
    public PropertyReviewsSummary getPropertyReviews(Long propertyId, int page, int size) {

        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByPropertyIdOrderByCreatedAtDesc(
            propertyId, pageable
        );

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return PropertyReviewsSummary.builder()
            .propertyId(propertyId)
            .propertyName(property.getName())
            .averageRating(property.getAverageRating())
            .totalReviews(property.getReviewCount().longValue())
            .recentReviews(reviews)
            .build();
    }

    /**
     * Owner responds to a review.
     */
    @Transactional
    public ReviewResponse addOwnerResponse(Long reviewId, OwnerResponseRequest request) {

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        review.setOwnerResponse(request.getResponse());
        review.setOwnerRespondedAt(LocalDateTime.now());

        return mapToResponse(reviewRepository.save(review));
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    @Transactional
    public void updatePropertyRating(Long propertyId) {
        Double avgRating = reviewRepository.calculateAverageRating(propertyId);
        Long count = reviewRepository.countByPropertyId(propertyId);

        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        property.setAverageRating(avgRating != null ? avgRating : 0.0);
        property.setReviewCount(count.intValue());

        propertyRepository.save(property);
    }

    private ReviewResponse mapToResponse(Review r) {
        List<String> photos = r.getPhotoUrls() != null
            ? Arrays.asList(r.getPhotoUrls().split(","))
            : List.of();

        return ReviewResponse.builder()
            .id(r.getId())
            .bookingId(r.getBooking().getId())
            .propertyId(r.getProperty().getId())
            .guestName(r.getGuestName())
            .overallRating(r.getOverallRating())
            .cleanlinessRating(r.getCleanlinessRating())
            .accuracyRating(r.getAccuracyRating())
            .communicationRating(r.getCommunicationRating())
            .locationRating(r.getLocationRating())
            .valueRating(r.getValueRating())
            .comment(r.getComment())
            .photoUrls(photos)
            .isVerifiedStay(r.getIsVerifiedStay())
            .createdAt(r.getCreatedAt())
            .ownerResponse(r.getOwnerResponse())
            .ownerRespondedAt(r.getOwnerRespondedAt())
            .build();
    }
}
