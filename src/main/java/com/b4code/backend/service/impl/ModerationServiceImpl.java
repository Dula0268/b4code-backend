package com.b4code.backend.service.impl;

import com.b4code.backend.dao.*;
import com.b4code.backend.dto.*;
import com.b4code.backend.models.enums.*;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.*;
import com.b4code.backend.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b4code.backend.models.Review;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationServiceImpl implements ModerationService {

    private final FlaggedReviewRepository reviewRepository;
    private final DisputeRepository disputeRepository;
    private final ModerationHistoryRepository historyRepository;
    private final ItemReviewRepository itemReviewRepository;

    // ── Reviews Queue

    @Override
    @Transactional(readOnly = true)
    public Page<FlaggedReviewDto> getFlaggedReviews(ReviewStatus status, FlagType flagType, Integer rating, String search, int page, int size) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return reviewRepository.findAllWithFilters(status, flagType, rating, term,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "flaggedAt")))
                .map(FlaggedReviewDto::fromEntity);
    }

    @Override
    @Transactional
    public FlaggedReviewDto approveReview(Long id, String adminNote) {
        FlaggedReview flaggedReview = findReviewOrThrow(id);
        flaggedReview.setStatus(ReviewStatus.APPROVED);
        if (adminNote != null && !adminNote.trim().isEmpty()) {
            flaggedReview.setAdminNote(adminNote);
        }
        log.info("Review id={} APPROVED, note='{}'", id, adminNote);
        FlaggedReviewDto dto = FlaggedReviewDto.fromEntity(reviewRepository.save(flaggedReview));
        saveHistory("#REV-" + id, ModerationAction.REVIEW_KEPT, "Content within Guidelines. " + (adminNote != null ? adminNote : ""));
        return dto;
    }

    @Override
    @Transactional
    public FlaggedReviewDto removeReview(Long id, String adminNote) {
        FlaggedReview flaggedReview = findReviewOrThrow(id);
        flaggedReview.setStatus(ReviewStatus.REMOVED);
        flaggedReview.setAdminNote(adminNote);
        
        Review review = flaggedReview.getReview();
        if (review != null) {
            review.setComment("[Removed by Admin] " + (adminNote != null ? adminNote : "Violation of policy"));
        } else if (flaggedReview.getItemReviewId() != null) {
            ItemReview itemReview = itemReviewRepository.findById(flaggedReview.getItemReviewId()).orElse(null);
            if (itemReview != null) {
                itemReview.setComment("[Removed by Admin] " + (adminNote != null ? adminNote : "Violation of policy"));
                itemReviewRepository.save(itemReview);
            }
        }
        
        log.info("Review id={} REMOVED, reason='{}'", id, adminNote);
        FlaggedReviewDto dto = FlaggedReviewDto.fromEntity(reviewRepository.save(flaggedReview));
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
    public void exportHistoryToCsv(ModerationAction action, String search,
                                    LocalDateTime from, LocalDateTime to,
                                    jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        Page<ModerationHistory> pageResult = historyRepository.findAllWithFilters(action, term, from, to,
                org.springframework.data.domain.Pageable.unpaged());

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"moderation-history-report.csv\"");

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("Case ID,Resolved Date,Action Taken,Resolved By,Outcome,Review ID,Booking ID,Property Name,Rating,Review Comment,Flagged By,Reason for Flag,Flag Date,Current Status,Admin Internal Notes");
            for (ModerationHistory h : pageResult.getContent()) {
                String reviewId = "N/A";
                String bookingId = "N/A";
                String propertyName = "N/A";
                String rating = "N/A";
                String comment = "N/A";
                String flaggedBy = "N/A";
                String reasonForFlag = "N/A";
                String flagDate = "N/A";
                String currentStatus = "N/A";
                String adminNotes = "N/A";

                if (h.getCaseId() != null && h.getCaseId().startsWith("#REV-")) {
                    try {
                        Long rId = Long.parseLong(h.getCaseId().substring(5));
                        FlaggedReview fr = reviewRepository.findById(rId).orElse(null);
                        if (fr != null) {
                            reviewId = fr.getReview() != null ? String.valueOf(fr.getReview().getId()) : (fr.getId() != null ? "FlaggedID:" + fr.getId() : "N/A");
                            bookingId = (fr.getReview() != null && fr.getReview().getBooking() != null) ? String.valueOf(fr.getReview().getBooking().getId()) : "N/A";
                            propertyName = fr.getProperty() != null ? fr.getProperty().getName() : (fr.getPropertyId() != null ? "Property ID: " + fr.getPropertyId() : ((fr.getReview() != null && fr.getReview().getProperty() != null) ? fr.getReview().getProperty().getName() : "N/A"));
                            rating = fr.getRating() != null ? String.valueOf(fr.getRating()) : (fr.getReview() != null ? String.valueOf(fr.getReview().getOverallRating()) : "N/A");
                            comment = fr.getReviewText() != null ? fr.getReviewText() : (fr.getReview() != null ? fr.getReview().getComment() : "N/A");
                            flaggedBy = fr.getOwner() != null ? String.valueOf(fr.getOwner().getId()) : "System";
                            reasonForFlag = fr.getFlagType() != null ? fr.getFlagType().name() : "N/A";
                            flagDate = fr.getFlaggedAt() != null ? fr.getFlaggedAt().toString() : "N/A";
                            currentStatus = fr.getStatus() != null ? fr.getStatus().name() : "N/A";
                            adminNotes = fr.getAdminNote() != null ? fr.getAdminNote() : "N/A";
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse review id from caseId={}", h.getCaseId());
                    }
                } else if (h.getCaseId() != null) {
                    // It's a dispute
                    Dispute d = disputeRepository.findAll().stream().filter(dis -> h.getCaseId().equals(dis.getDisputeId())).findFirst().orElse(null);
                    if (d != null) {
                        bookingId = d.getBooking() != null ? String.valueOf(d.getBooking().getId()) : "N/A";
                        propertyName = d.getProperty() != null ? d.getProperty().getName() : "N/A";
                        reasonForFlag = "Dispute";
                        currentStatus = d.getStatus() != null ? d.getStatus().name() : "N/A";
                        adminNotes = d.getInternalNote() != null ? d.getInternalNote() : "N/A";
                    }
                }

                String outcome = h.getOutcome() != null ? h.getOutcome().replace("\"", "\"\"").replace("\n", " ") : "";
                comment = comment != null ? comment.replace("\"", "\"\"").replace("\n", " ") : "";
                adminNotes = adminNotes != null ? adminNotes.replace("\"", "\"\"").replace("\n", " ") : "";

                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        h.getCaseId(),
                        h.getResolvedAt() != null ? h.getResolvedAt().toString() : "",
                        h.getActionTaken(),
                        h.getAdmin() != null ? h.getAdmin().getFirstName() + " " + h.getAdmin().getLastName() : "System",
                        outcome,
                        reviewId,
                        bookingId,
                        propertyName,
                        rating,
                        comment,
                        flaggedBy,
                        reasonForFlag,
                        flagDate,
                        currentStatus,
                        adminNotes
                );
            }
        }
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

    @Override
    @Transactional(readOnly = true)
    public long getRemovedTodayCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return historyRepository.countByActionTakenAndResolvedAtAfter(ModerationAction.REVIEW_REMOVED, startOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public long getResolvedDisputesCount() {
        return disputeRepository.countByStatus(DisputeStatus.RESOLVED);
    }

    @Override
    @Transactional(readOnly = true)
    public java.math.BigDecimal getTotalResolvedAmount() {
        return disputeRepository.sumAmountByStatus(DisputeStatus.RESOLVED);
    }

    private FlaggedReview findReviewOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException("Review id=" + id + " not found.", HttpStatus.NOT_FOUND));
    }

    private void saveHistory(String caseId, ModerationAction action, String outcome) {
        ModerationHistory h = new ModerationHistory();
        h.setCaseId(caseId);
        h.setActionTaken(action);
        // Admin fields mapped directly to User entity
        h.setOutcome(outcome);
        h.setResolvedAt(LocalDateTime.now());
        historyRepository.save(h);
    }
}
