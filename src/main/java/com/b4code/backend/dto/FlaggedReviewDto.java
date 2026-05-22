package com.b4code.backend.dto;

import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.models.FlaggedReview;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlaggedReviewDto {

    private Long id;
    private Long propertyId;
    private String propertyName;
    private Long guestId;
    private String guestName;
    private String guestInitial;
    private String guestAvatarColor;
    private String reviewText;
    private Double rating;
    private String flagReason;
    private String status;          
    private String adminNote;
    private String flaggedAt;       

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public static FlaggedReviewDto fromEntity(FlaggedReview r) {
        return FlaggedReviewDto.builder()
                .id(r.getId())
                .propertyId(r.getPropertyId())
                .propertyName(r.getPropertyName())
                .guestId(r.getGuestId())
                .guestName(r.getGuestName())
                .guestInitial(r.getGuestInitial())
                .guestAvatarColor(r.getGuestAvatarColor())
                .reviewText(r.getReviewText())
                .rating(r.getRating())
                .flagReason(r.getFlagReason())
                .status(toLabel(r.getStatus()))
                .adminNote(r.getAdminNote())
                .flaggedAt(r.getFlaggedAt() != null ? r.getFlaggedAt().format(FMT) : "")
                .build();
    }

    private static String toLabel(ReviewStatus s) {
        return switch (s) {
            case FLAGGED  -> "Flagged";
            case APPROVED -> "Approved";
            case REMOVED  -> "Removed";
        };
    }
}

