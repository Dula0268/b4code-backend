package com.b4code.backend.service;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.ReviewRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.ReviewDTO;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;

    @BeforeEach
    void setUp() {
        Property property = new Property();
        property.setId(10L);
        property.setName("Test Property");

        review = new Review();
        review.setId(1L);
        review.setOverallRating(5);
        review.setComment("Great stay!");
        review.setProperty(property);
        review.setVisibilityStatus("PUBLIC");
    }

    @Test
    void testGetPropertyReviews() {
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(review.getProperty()));
        Page<Review> reviewPage = new PageImpl<>(List.of(review));
        when(reviewRepository.findByPropertyIdOrderByCreatedAtDesc(any(), any())).thenReturn(reviewPage);

        ReviewDTO.PropertyReviewsSummary summary = reviewService.getPropertyReviews(10L, 0, 10);

        assertEquals(1, summary.getRecentReviews().size());
        assertEquals("Great stay!", summary.getRecentReviews().get(0).getComment());
    }

    @Test
    void testReplyToReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        ReviewDTO.ReviewResponse response = reviewService.replyToReview(1L, "Thank you for staying with us!");

        assertEquals("Thank you for staying with us!", response.getReplyText());
    }

    @Test
    void testFlagReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        ReviewDTO.ReviewResponse response = reviewService.flagReview(1L);

        assertEquals("FLAGGED", response.getVisibilityStatus());
    }
}
