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
    private final NotificationService notificationService;
    private final AdminNotificationService adminNotificationService;

    /**
     * Create a review (guests can only review completed bookings).
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null 
            ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() 
            : null;

        if (currentUserEmail == null) {
            throw new IllegalStateException("User must be logged in to review");
        }

        User guest = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Guest user not found"));

        // Enforce booking belongs to user
        if (!guest.getEmail().equals(booking.getGuestEmail())) {
            throw new IllegalStateException("You can only review your own bookings");
        }

        // Enforce booking is completed
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("You can only review completed stays");
        }

        String photoUrlsStr = request.getPhotoUrls() != null
            ? String.join(",", request.getPhotoUrls())
            : null;

        Review review = reviewRepository.findByBookingId(booking.getId()).orElseGet(() -> {
            return Review.builder()
                .booking(booking)
                .property(booking.getRoomType().getProperty())
                .guest(guest)
                .build();
        });

        review.setOverallRating(request.getOverallRating());
        review.setCleanlinessRating(request.getCleanlinessRating());
        review.setComfortRating(request.getComfortRating());
        review.setServiceRating(request.getServiceRating());
        review.setDiningRating(request.getDiningRating());
        review.setLocationRating(request.getLocationRating());
        review.setValueRating(request.getValueRating());
        review.setComment(request.getComment());
        review.setPhotoUrls(photoUrlsStr);

        Review saved = reviewRepository.save(review);

        // Update property average rating
        updatePropertyRating(booking.getRoomType().getProperty().getId());

        // Send Notification
        notificationService.createNotification(guest, 
            "Review Submitted", 
            "Thank you for reviewing your stay at " + booking.getRoomType().getProperty().getName() + "!"
        );

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
            .averageRating(property.getAverageRating() != null ? property.getAverageRating() : 0.0)
            .totalReviews(property.getReviewCount() != null ? property.getReviewCount().longValue() : 0L)
            .avgCleanliness(property.getAvgCleanliness())
            .avgComfort(property.getAvgComfort())
            .avgService(property.getAvgService())
            .avgDining(property.getAvgDining())
            .avgLocation(property.getAvgLocation())
            .avgValue(property.getAvgValue())
            .recentReviews(reviews)
            .build();
    }



    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    @Transactional
    public void updatePropertyRating(Long propertyId) {
        Property property = propertyRepository.findById(propertyId).orElse(null);
        if (property == null) return;
        
        List<Review> reviews = reviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId, Pageable.unpaged()).getContent();
        if (reviews.isEmpty()) return;

        double total = 0, clean = 0, comfort = 0, service = 0, dining = 0, loc = 0, val = 0;
        int cleanC = 0, comfortC = 0, serviceC = 0, diningC = 0, locC = 0, valC = 0;

        for (Review r : reviews) {
            total += r.getOverallRating();
            if (r.getCleanlinessRating() != null) { clean += r.getCleanlinessRating(); cleanC++; }
            if (r.getComfortRating() != null) { comfort += r.getComfortRating(); comfortC++; }
            if (r.getServiceRating() != null) { service += r.getServiceRating(); serviceC++; }
            if (r.getDiningRating() != null) { dining += r.getDiningRating(); diningC++; }
            if (r.getLocationRating() != null) { loc += r.getLocationRating(); locC++; }
            if (r.getValueRating() != null) { val += r.getValueRating(); valC++; }
        }

        property.setAverageRating(total / reviews.size());
        property.setReviewCount(reviews.size());
        property.setAvgCleanliness(cleanC > 0 ? clean / cleanC : null);
        property.setAvgComfort(comfortC > 0 ? comfort / comfortC : null);
        property.setAvgService(serviceC > 0 ? service / serviceC : null);
        property.setAvgDining(diningC > 0 ? dining / diningC : null);
        property.setAvgLocation(locC > 0 ? loc / locC : null);
        property.setAvgValue(valC > 0 ? val / valC : null);

        propertyRepository.save(property);
    }

    @Transactional
    public ReviewResponse replyToReview(Long reviewId, String replyText) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setReplyText(replyText);
        review.setReplyCreatedAt(LocalDateTime.now());
        return mapToResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse flagReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setVisibilityStatus("FLAGGED");
        
        // Notify Admin
        adminNotificationService.createNotification(
            "Review Flagged",
            "A review for property '" + review.getProperty().getName() + "' has been flagged.",
            com.b4code.backend.models.enums.AdminNotificationType.FLAGGED_REVIEW,
            review.getId().toString()
        );

        return mapToResponse(reviewRepository.save(review));
    }

    private ReviewResponse mapToResponse(Review r) {
        List<String> photos = r.getPhotoUrls() != null && !r.getPhotoUrls().isEmpty()
            ? Arrays.asList(r.getPhotoUrls().split(","))
            : List.of();

        return ReviewResponse.builder()
            .id(r.getId())
            .bookingId(r.getBooking() != null ? r.getBooking().getId() : null)
            .propertyId(r.getProperty() != null ? r.getProperty().getId() : null)
            .guestId(r.getGuest() != null ? r.getGuest().getId() : null)
            .overallRating(r.getOverallRating())
            .cleanlinessRating(r.getCleanlinessRating())
            .comfortRating(r.getComfortRating())
            .serviceRating(r.getServiceRating())
            .diningRating(r.getDiningRating())
            .locationRating(r.getLocationRating())
            .valueRating(r.getValueRating())
            .comment(r.getComment())
            .photoUrls(photos)
            .replyText(r.getReplyText())
            .visibilityStatus(r.getVisibilityStatus())
            .createdAt(r.getCreatedAt())
            .build();
    }
}
