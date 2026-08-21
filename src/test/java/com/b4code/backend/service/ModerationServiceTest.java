package com.b4code.backend.service;

import com.b4code.backend.dao.DisputeRepository;
import com.b4code.backend.dao.FlaggedReviewRepository;
import com.b4code.backend.dao.ItemReviewRepository;
import com.b4code.backend.dao.ModerationHistoryRepository;
import com.b4code.backend.dto.FlaggedReviewDto;
import com.b4code.backend.models.FlaggedReview;
import com.b4code.backend.models.ModerationHistory;
import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.service.impl.ModerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModerationServiceTest {

    @Mock
    private FlaggedReviewRepository reviewRepository;

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private ModerationHistoryRepository historyRepository;

    @Mock
    private ItemReviewRepository itemReviewRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ModerationServiceImpl moderationService;

    @Test
    void testApproveReview() {
        FlaggedReview flaggedReview = new FlaggedReview();
        flaggedReview.setId(1L);
        flaggedReview.setStatus(ReviewStatus.FLAGGED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(flaggedReview));
        when(reviewRepository.save(any())).thenReturn(flaggedReview);

        FlaggedReviewDto result = moderationService.approveReview(1L, "Looks good");

        assertNotNull(result);
        assertEquals(ReviewStatus.APPROVED, flaggedReview.getStatus());
        assertEquals("Looks good", flaggedReview.getAdminNote());
        verify(historyRepository, times(1)).save(any(ModerationHistory.class));
    }

    @Test
    void testRemoveReview() {
        FlaggedReview flaggedReview = new FlaggedReview();
        flaggedReview.setId(1L);
        flaggedReview.setStatus(ReviewStatus.FLAGGED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(flaggedReview));
        when(reviewRepository.save(any())).thenReturn(flaggedReview);

        FlaggedReviewDto result = moderationService.removeReview(1L, "Violation");

        assertNotNull(result);
        assertEquals(ReviewStatus.REMOVED, flaggedReview.getStatus());
        assertEquals("Violation", flaggedReview.getAdminNote());
        verify(historyRepository, times(1)).save(any(ModerationHistory.class));
    }
}
