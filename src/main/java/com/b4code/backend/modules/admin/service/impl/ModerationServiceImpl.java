package com.b4code.backend.modules.admin.service.impl;

import com.b4code.backend.modules.admin.dao.*;
import com.b4code.backend.modules.admin.dto.*;
import com.b4code.backend.modules.admin.enums.*;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.modules.admin.models.*;
import com.b4code.backend.modules.admin.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationServiceImpl implements ModerationService {

    private final FlaggedReviewRepository reviewRepository;
    private final DisputeRepository disputeRepository;
    private final ModerationHistoryRepository historyRepository;

    // ── Reviews Queue

    @Override
    @Transactional(readOnly = true)
    public Page<FlaggedReviewDto> getFlaggedReviews(ReviewStatus status, String flagReason, Double rating, String search, int page, int size) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        String reasonFilter = (flagReason == null || flagReason.isBlank()) ? null : flagReason.trim();
        return reviewRepository.findAllWithFilters(status, reasonFilter, rating, term,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "flaggedAt")))
                .map(FlaggedReviewDto::fromEntity);
    }

    @Override
    @Transactional
    public FlaggedReviewDto approveReview(Long id) {
        FlaggedReview review = findReviewOrThrow(id);
        review.setStatus(ReviewStatus.APPROVED);
        log.info("Review id={} APPROVED", id);
        FlaggedReviewDto dto = FlaggedReviewDto.fromEntity(reviewRepository.save(review));
        saveHistory("#REV-" + id, ModerationAction.REVIEW_KEPT, "Content within Guidelines");
        return dto;
    }

    @Override
    @Transactional
    public FlaggedReviewDto removeReview(Long id, String adminNote) {
        FlaggedReview review = findReviewOrThrow(id);
        review.setStatus(ReviewStatus.REMOVED);
        review.setAdminNote(adminNote);
        log.info("Review id={} REMOVED, reason='{}'", id, adminNote);
        FlaggedReviewDto dto = FlaggedReviewDto.fromEntity(reviewRepository.save(review));
        saveHistory("#REV-" + id, ModerationAction.REVIEW_REMOVED, adminNote);
        return dto;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<DisputeDto> getDisputes(DisputeStatus status, String search, int page, int size) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return disputeRepository.findAllWithFilters(status, term,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "openedAt")))
                .map(DisputeDto::fromEntity);
    }

    @Override
    @Transactional
    public DisputeDto resolveDispute(Long id, String resolution, boolean refundApproved) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Dispute id=" + id + " not found.", HttpStatus.NOT_FOUND));
        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolutionNote(resolution);
        log.info("Dispute id={} RESOLVED — refundApproved={}", id, refundApproved);
        DisputeDto dto = DisputeDto.fromEntity(disputeRepository.save(dispute));
        ModerationAction action = refundApproved ? ModerationAction.REFUND_ISSUED : ModerationAction.APPEAL_DENIED;
        saveHistory(dispute.getDisputeId(), action, resolution);
        return dto;
    }

    @Override
    @Transactional
    public DisputeDto saveDisputeNote(Long id, String note) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Dispute id=" + id + " not found.", HttpStatus.NOT_FOUND));
        dispute.setInternalNote(note);
        log.info("Internal note saved for dispute id={}", id);
        return DisputeDto.fromEntity(disputeRepository.save(dispute));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ModerationHistoryDto> getHistory(ModerationAction action, String search,
                                                  LocalDateTime from, LocalDateTime to,
                                                  int page, int size) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return historyRepository.findAllWithFilters(action, term, from, to,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "resolvedAt")))
                .map(ModerationHistoryDto::fromEntity);
    }


    @Override
    @Transactional(readOnly = true)
    public long getPendingReviewCount() {
        return reviewRepository.countByStatus(ReviewStatus.FLAGGED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getOpenDisputeCount() {
        return disputeRepository.countByStatusNot(DisputeStatus.RESOLVED);
    }


    private FlaggedReview findReviewOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException("Review id=" + id + " not found.", HttpStatus.NOT_FOUND));
    }

    private void saveHistory(String caseId, ModerationAction action, String outcome) {
        ModerationHistory h = new ModerationHistory();
        h.setCaseId(caseId);
        h.setActionTaken(action);
        h.setAdminName("System");   
        h.setAdminInitials("SYS");
        h.setAdminColor("#C05621");
        h.setOutcome(outcome);
        h.setResolvedAt(LocalDateTime.now());
        historyRepository.save(h);
    }
}
