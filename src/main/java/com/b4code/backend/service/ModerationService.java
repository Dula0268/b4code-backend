package com.b4code.backend.service;

import com.b4code.backend.dto.*;
import com.b4code.backend.models.enums.DisputeStatus;
import com.b4code.backend.models.enums.ModerationAction;
import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.models.enums.FlagType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ModerationService {

    Page<FlaggedReviewDto> getFlaggedReviews(ReviewStatus status, FlagType flagType, Integer rating, String search, int page, int size);
    FlaggedReviewDto getFlaggedReviewById(Long id);
    FlaggedReviewDto approveReview(Long id, String adminNote);
    FlaggedReviewDto removeReview(Long id, String adminNote);

    Page<DisputeDto> getDisputes(DisputeStatus status, String search, Boolean isComplaint, int page, int size);
    DisputeDto resolveDispute(Long id, String resolution, boolean refundApproved);
    DisputeDto saveDisputeNote(Long id, String note);

    Page<ModerationHistoryDto> getHistory(ModerationAction action, String search,
                                           LocalDateTime from, LocalDateTime to,
                                           int page, int size);

    void exportHistoryToCsv(ModerationAction action, String search,
                            LocalDateTime from, LocalDateTime to,
                            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException;

    long getPendingReviewCount();
    long getOpenDisputeCount();
    long getRemovedTodayCount();
    long getResolvedDisputesCount();
    java.math.BigDecimal getTotalResolvedAmount();
}

