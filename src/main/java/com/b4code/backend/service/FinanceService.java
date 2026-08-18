package com.b4code.backend.service;

import com.b4code.backend.dto.*;
import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.models.enums.RefundStatus;
import com.b4code.backend.models.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public interface FinanceService {

    FinanceSummaryDto getFinanceSummary();

    List<RevenueTrendPointDto> getRevenueTrend(String timeframe);

    TransactionPageDto getAllTransactions(String search, TransactionType type, LocalDateTime from, LocalDateTime to,
            int page, int size);

    TransactionDto getTransactionById(Long id);

    RefundPageDto getAllRefunds(String search, RefundStatus status, int page, int size);

    RefundDto approveRefund(Long id);

    RefundDto rejectRefund(Long id, String adminNote);

    PayoutPageDto getAllPayouts(String search, PayoutStatus status, int page, int size);

    PayoutDto processPayout(Long id, String bankReference, java.math.BigDecimal commissionRate);

    PayoutDto rejectPayout(Long id);

    java.math.BigDecimal getCommissionRate();

    java.math.BigDecimal updateCommissionRate(java.math.BigDecimal newRate);

    void exportPayoutsToCsv(String search, PayoutStatus status, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException;
}
