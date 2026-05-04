package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.modules.guest.entity.Review;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailDto {

    private Long id;
    private String author;
    private String avatarInitials;
    private String avatarColor;
    private String date;
    private String text;
    private Integer rating;
    private String ownerReply;

    public static ReviewDetailDto fromEntity(Review review) {
        String initials = review.getGuestName() != null && !review.getGuestName().isEmpty()
                ? review.getGuestName().substring(0, Math.min(2, review.getGuestName().length())).toUpperCase()
                : "?";
        
        String[] colors = {
            "#f4a261", "#2f80ed", "#e84393", "#27ae60", "#9b59b6", "#e67e22"
        };
        String color = colors[(int)(review.getId() % colors.length)];

        return ReviewDetailDto.builder()
                .id(review.getId())
                .author(review.getGuestName())
                .avatarInitials(initials)
                .avatarColor(color)
                .date(review.getCreatedAt() != null ? review.getCreatedAt().toString().substring(0, 10) : "")
                .text(review.getReviewText())
                .rating(review.getRating().intValue())
                .build();
    }
}
