package com.b4code.backend.modules.admin;

import com.b4code.backend.modules.admin.dao.PayoutRepository;
import com.b4code.backend.modules.admin.dao.RefundRepository;
import com.b4code.backend.modules.admin.dao.TransactionRepository;
import com.b4code.backend.modules.admin.enums.PayoutStatus;
import com.b4code.backend.modules.admin.enums.RefundStatus;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.modules.admin.models.Payout;
import com.b4code.backend.modules.admin.models.Refund;
import com.b4code.backend.modules.admin.service.impl.FinanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FinanceServiceImpl.
 *
 * HOW TO RUN:
 *   cd "Backend/b4code-backend"
 *   ./mvnw test -Dtest=FinanceServiceTest
 */
@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock PayoutRepository payoutRepository;
    @Mock RefundRepository refundRepository;
    @Mock TransactionRepository transactionRepository;

    @InjectMocks FinanceServiceImpl service;

    private Payout pendingPayout;
    private Refund pendingRefund;

    @BeforeEach
    void setUp() {
        pendingPayout = new Payout();
        pendingPayout.setId(10L);
        pendingPayout.setOwnerName("Nina Patel");
        pendingPayout.setAmount(BigDecimal.valueOf(40800.00));
        pendingPayout.setStatus(PayoutStatus.PENDING);

        pendingRefund = new Refund();
        pendingRefund.setId(5L);
        pendingRefund.setAmount(BigDecimal.valueOf(5000.00));
        pendingRefund.setStatus(RefundStatus.PENDING);
    }

    // ─── processPayout ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("processPayout: sets status to PROCESSED and saves bankReference")
    void processPayout_setsProcessedStatusAndBankRef() {
        when(payoutRepository.findById(10L)).thenReturn(Optional.of(pendingPayout));
        when(payoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processPayout(10L, "BOC-REF-2024");

        assertThat(pendingPayout.getStatus()).isEqualTo(PayoutStatus.PROCESSED);
        assertThat(pendingPayout.getBankReference()).isEqualTo("BOC-REF-2024");
        verify(payoutRepository).save(pendingPayout);
    }

    @Test
    @DisplayName("processPayout: accepts empty bankReference without error")
    void processPayout_acceptsEmptyBankRef() {
        when(payoutRepository.findById(10L)).thenReturn(Optional.of(pendingPayout));
        when(payoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processPayout(10L, "");

        assertThat(pendingPayout.getStatus()).isEqualTo(PayoutStatus.PROCESSED);
        assertThat(pendingPayout.getBankReference()).isEmpty();
    }

    @Test
    @DisplayName("processPayout: throws when payout not found")
    void processPayout_throwsWhenNotFound() {
        when(payoutRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processPayout(999L, "REF"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("999");
    }

    // ─── approveRefund ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("approveRefund: sets status to APPROVED")
    void approveRefund_setsApproved() {
        when(refundRepository.findById(5L)).thenReturn(Optional.of(pendingRefund));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveRefund(5L);

        assertThat(pendingRefund.getStatus()).isEqualTo(RefundStatus.APPROVED);
    }

    @Test
    @DisplayName("approveRefund: throws when refund not found")
    void approveRefund_throwsWhenNotFound() {
        when(refundRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveRefund(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("999");
    }

    // ─── rejectRefund ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("rejectRefund: sets status to REJECTED and saves adminNote")
    void rejectRefund_setsRejectedWithNote() {
        when(refundRepository.findById(5L)).thenReturn(Optional.of(pendingRefund));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.rejectRefund(5L, "Duplicate request");

        assertThat(pendingRefund.getStatus()).isEqualTo(RefundStatus.REJECTED);
        assertThat(pendingRefund.getAdminNote()).isEqualTo("Duplicate request");
    }

    // ─── getFinanceSummary ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getFinanceSummary: builds summary from repository aggregates")
    void getFinanceSummary_buildsFromRepositories() {
        when(transactionRepository.sumTotalRevenue()).thenReturn(BigDecimal.valueOf(100000));
        when(transactionRepository.sumPlatformCommission()).thenReturn(BigDecimal.valueOf(10000));
        when(payoutRepository.sumProcessedPayouts()).thenReturn(BigDecimal.valueOf(85000));
        when(refundRepository.sumApprovedRefunds()).thenReturn(BigDecimal.valueOf(5000));

        var summary = service.getFinanceSummary();

        assertThat(summary.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(summary.getPlatformCommission()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(summary.getTotalPayouts()).isEqualByComparingTo(BigDecimal.valueOf(85000));
        assertThat(summary.getCurrency()).isEqualTo("LKR");
    }
}
