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
    private String flagType;
    private Long ownerId;
    private String ownerName;
    private String flaggedByRole;
    private String status;          
    private String adminNote;
    private String flaggedAt;       

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public static FlaggedReviewDto fromEntity(FlaggedReview r) {
        String propName = r.getProperty() != null ? r.getProperty().getName() : (r.getReview() != null && r.getReview().getProperty() != null ? r.getReview().getProperty().getName() : (r.getPropertyId() != null ? "Property ID: " + r.getPropertyId() : null));
        Long propId = r.getPropertyId() != null ? r.getPropertyId() : (r.getReview() != null && r.getReview().getProperty() != null ? r.getReview().getProperty().getId() : null);
        String gName = r.getGuestName() != null ? r.getGuestName() : (r.getReview() != null && r.getReview().getGuest() != null ? r.getReview().getGuest().getFullName() : null);
        String initial = gName != null && !gName.isEmpty() ? gName.substring(0, 1) : null;
        String text = r.getReviewText() != null ? r.getReviewText() : (r.getReview() != null ? r.getReview().getComment() : null);
        Double ratingVal = r.getRating() != null ? r.getRating() : (r.getReview() != null && r.getReview().getOverallRating() != null ? Double.valueOf(r.getReview().getOverallRating()) : null);

        return FlaggedReviewDto.builder()
                .id(r.getId())
                .propertyId(propId)
                .propertyName(propName)
                .guestId(r.getReview() != null && r.getReview().getGuest() != null ? r.getReview().getGuest().getId() : null)
                .guestName(gName)
                .guestInitial(initial)
                .guestAvatarColor(r.getReview() != null && r.getReview().getGuest() != null ? r.getReview().getGuest().getAvatarUrl() : null)
                .reviewText(text)
                .rating(ratingVal)
                .flagType(r.getFlagType() != null ? r.getFlagType().name() : null)
                .ownerId(r.getOwner() != null ? r.getOwner().getId() : null)
                .ownerName(r.getOwner() != null ? r.getOwner().getFullName() : null)
                .flaggedByRole(r.getOwner() != null && r.getOwner().getRole() != null ? r.getOwner().getRole().name() : null)
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

