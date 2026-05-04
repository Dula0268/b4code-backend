package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.modules.guest.entity.Review;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private Long id;
    private Long propertyId;
    private Long guestId;
    private String guestName;
    private String reviewText;
    private Double rating;
    private String createdAt;

    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .propertyId(review.getPropertyId())
                .guestId(review.getGuestId())
                .guestName(review.getGuestName())
                .reviewText(review.getReviewText())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .build();
    }
}
