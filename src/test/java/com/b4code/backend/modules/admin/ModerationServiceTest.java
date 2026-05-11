package com.b4code.backend.modules.admin;

import com.b4code.backend.modules.admin.dao.FlaggedReviewRepository;
import com.b4code.backend.modules.admin.dao.ModerationHistoryRepository;
import com.b4code.backend.modules.admin.dao.DisputeRepository;
import com.b4code.backend.modules.admin.dto.FlaggedReviewDto;
import com.b4code.backend.modules.admin.enums.ModerationAction;
import com.b4code.backend.modules.admin.enums.ReviewStatus;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.modules.admin.models.FlaggedReview;
import com.b4code.backend.modules.admin.models.ModerationHistory;
import com.b4code.backend.modules.admin.service.impl.ModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ModerationServiceImpl.
 *
 * HOW TO RUN:
 *   cd "Backend/b4code-backend"
 *   ./mvnw test -Dtest=ModerationServiceTest
 *   (or run all: ./mvnw test)
 */
@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock FlaggedReviewRepository reviewRepository;
    @Mock ModerationHistoryRepository historyRepository;
    @Mock DisputeRepository disputeRepository;

    @InjectMocks ModerationServiceImpl service;

    private FlaggedReview flaggedReview;

    @BeforeEach
    void setUp() {
        flaggedReview = new FlaggedReview();
        flaggedReview.setId(1L);
        flaggedReview.setGuestName("Alice Smith");
        flaggedReview.setPropertyName("Oceanview Villa");
        flaggedReview.setReviewText("Bugs everywhere!");
        flaggedReview.setRating(1.0);
        flaggedReview.setFlagReason("Inappropriate Content");
        flaggedReview.setStatus(ReviewStatus.FLAGGED);
    }

    // ─── approveReview ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("approveReview: sets status to APPROVED and saves history")
    void approveReview_setsApprovedStatusAndSavesHistory() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(flaggedReview));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveReview(1L);

        // Verify status changed
        assertThat(flaggedReview.getStatus()).isEqualTo(ReviewStatus.APPROVED);

        // Verify history saved with correct action
        ArgumentCaptor<ModerationHistory> histCaptor = ArgumentCaptor.forClass(ModerationHistory.class);
        verify(historyRepository).save(histCaptor.capture());
        assertThat(histCaptor.getValue().getActionTaken()).isEqualTo(ModerationAction.REVIEW_KEPT);
        assertThat(histCaptor.getValue().getCaseId()).isEqualTo("#REV-1");
    }

    @Test
    @DisplayName("approveReview: throws when review not found")
    void approveReview_throwsWhenNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.approveReview(99L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("99");
    }

    // ─── removeReview ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeReview: sets status to REMOVED, saves adminNote, and saves history")
    void removeReview_setsRemovedStatusAndSavesHistory() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(flaggedReview));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.removeReview(1L, "Policy violation");

        assertThat(flaggedReview.getStatus()).isEqualTo(ReviewStatus.REMOVED);
        assertThat(flaggedReview.getAdminNote()).isEqualTo("Policy violation");

        ArgumentCaptor<ModerationHistory> histCaptor = ArgumentCaptor.forClass(ModerationHistory.class);
        verify(historyRepository).save(histCaptor.capture());
        assertThat(histCaptor.getValue().getActionTaken()).isEqualTo(ModerationAction.REVIEW_REMOVED);
        assertThat(histCaptor.getValue().getOutcome()).isEqualTo("Policy violation");
    }

    // ─── getFlaggedReviews ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getFlaggedReviews: returns page of FLAGGED reviews only")
    void getFlaggedReviews_returnsOnlyFlagged() {
        Page<FlaggedReview> page = new PageImpl<>(List.of(flaggedReview));
        when(reviewRepository.findAllWithFilters(
                eq(ReviewStatus.FLAGGED), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        var result = service.getFlaggedReviews(ReviewStatus.FLAGGED, null, null, null, 0, 10);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("FLAGGED");
    }

    // ─── getPendingReviewCount ───────────────────────────────────────────────────

    @Test
    @DisplayName("getPendingReviewCount: delegates to countByStatus(FLAGGED)")
    void getPendingReviewCount_returnsCorrectCount() {
        when(reviewRepository.countByStatus(ReviewStatus.FLAGGED)).thenReturn(3L);
        assertThat(service.getPendingReviewCount()).isEqualTo(3L);
    }
}
