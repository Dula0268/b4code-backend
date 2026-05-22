package com.b4code.backend.modules.admin.dto;

import com.b4code.backend.models.enums.ModerationAction;
import com.b4code.backend.models.ModerationHistory;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModerationHistoryDto {

    private String id;
    private String resolvedDate;        
    private String resolvedTime;        
    private String caseId;             
    private String actionTaken;        
    private String adminInitials;
    private String adminName;
    private String adminColor;
    private String outcome;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm a");

    public static ModerationHistoryDto fromEntity(ModerationHistory h) {
        return ModerationHistoryDto.builder()
                .id(String.valueOf(h.getId()))
                .resolvedDate(h.getResolvedAt() != null ? h.getResolvedAt().format(DATE_FMT) : "")
                .resolvedTime(h.getResolvedAt() != null ? h.getResolvedAt().format(TIME_FMT) : "")
                .caseId(h.getCaseId())
                .actionTaken(toLabel(h.getActionTaken()))
                .adminInitials(h.getAdminInitials())
                .adminName(h.getAdminName())
                .adminColor(h.getAdminColor())
                .outcome(h.getOutcome())
                .build();
    }

    private static String toLabel(ModerationAction a) {
        return switch (a) {
            case REVIEW_REMOVED -> "Review Removed";
            case REFUND_ISSUED  -> "Refund Issued";
            case REVIEW_KEPT    -> "Review Kept";
            case APPEAL_DENIED  -> "Appeal Denied";
        };
    }
}
