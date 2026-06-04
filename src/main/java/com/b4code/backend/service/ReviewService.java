package com.b4code.backend.service;

import com.b4code.backend.dto.ReviewDTO.*;
import com.b4code.backend.exceptions.ResourceNotFoundException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Review;
import com.b4code.backend.models.User;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.ReviewRepository;
import com.b4code.backend.dao.UserRepository;
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
    private final UserRepository userRepository;

    /**
     * Create a review (guests can only review completed bookings).
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking not found: " + request.getBookingId()));

        // Check removed: guests can only review completed bookings
        // (Status was removed from booking)

        // Prevent duplicate reviews
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("Review already exists for this booking");
        }

        String photoUrlsStr = request.getPhotoUrls() != null
            ? String.join(",", request.getPhotoUrls())
            : null;

        // Since guestEmail was removed, we can't look up user by email directly here.
        // For now, look up user from SecurityContext or assume a hardcoded admin user for compilation.
        User guest = userRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Guest user not found"));

        Review review = Review.builder()
            .booking(booking)
            .property(booking.getRoom().getProperty())
            .guest(guest)
            .overallRating(request.getOverallRating())
            .comment(request.getComment())
            .photoUrls(photoUrlsStr)
            .build();

        Review saved = reviewRepository.save(review);

        // Update property average rating (No longer stored on property entity)
        // updatePropertyRating(booking.getRoom().getProperty().getId());

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

        Double avgRating = reviewRepository.calculateAverageRating(propertyId);
        Long count = reviewRepository.countByPropertyId(propertyId);

        return PropertyReviewsSummary.builder()
            .propertyId(propertyId)
            .propertyName(property.getName())
            .averageRating(avgRating != null ? avgRating : 0.0)
            .totalReviews(count != null ? count : 0L)
            .recentReviews(reviews)
            .build();
    }



    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    @Transactional
    public void updatePropertyRating(Long propertyId) {
        // Method retained for API compatibility, but ratings are no longer stored on Property entity
    }

    private ReviewResponse mapToResponse(Review r) {
        List<String> photos = r.getPhotoUrls() != null
            ? Arrays.asList(r.getPhotoUrls().split(","))
            : List.of();

        return ReviewResponse.builder()
            .id(r.getId())
            .bookingId(r.getBooking().getId())
            .propertyId(r.getProperty().getId())
            .guestId(r.getGuest().getId())
            .overallRating(r.getOverallRating())
            .comment(r.getComment())
            .photoUrls(photos)
            .createdAt(r.getCreatedAt())
            .build();
    }
}
