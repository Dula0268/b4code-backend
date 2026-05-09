package com.b4code.backend.modules.admin.service;

import com.b4code.backend.modules.admin.dto.*;
import com.b4code.backend.modules.admin.enums.DisputeStatus;
import com.b4code.backend.modules.admin.enums.ModerationAction;
import com.b4code.backend.modules.admin.enums.ReviewStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ModerationService {

    Page<FlaggedReviewDto> getFlaggedReviews(ReviewStatus status, String search, int page, int size);
    FlaggedReviewDto approveReview(Long id);
    FlaggedReviewDto removeReview(Long id, String adminNote);

    Page<DisputeDto> getDisputes(DisputeStatus status, String search, int page, int size);
    DisputeDto resolveDispute(Long id, String resolution, boolean refundApproved);

    Page<ModerationHistoryDto> getHistory(ModerationAction action, String search,
                                           LocalDateTime from, LocalDateTime to,
                                           int page, int size);

    long getPendingReviewCount();
    long getOpenDisputeCount();
}
