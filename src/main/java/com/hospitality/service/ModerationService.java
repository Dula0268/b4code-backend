package com.hospitality.service;

import com.hospitality.dto.admin.*;
import com.hospitality.enums.DisputeStatus;
import com.hospitality.enums.ModerationAction;
import com.hospitality.enums.ReviewStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ModerationService {

    Page<FlaggedReviewDto> getFlaggedReviews(ReviewStatus status, String flagReason, String search, int page, int size);
    FlaggedReviewDto approveReview(Long id);
    FlaggedReviewDto removeReview(Long id, String adminNote);

    Page<DisputeDto> getDisputes(DisputeStatus status, String search, int page, int size);
    DisputeDto resolveDispute(Long id, String resolution, boolean refundApproved);

    Page<ModerationHistoryDto> getHistory(ModerationAction action, String search,
                                           LocalDateTime from, LocalDateTime to,
                                           int page, int size);

    long getPendingReviewCount();
    long getOpenDisputeCount();
    long getRemovedTodayCount();
}
