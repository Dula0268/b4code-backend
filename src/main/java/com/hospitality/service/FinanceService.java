package com.hospitality.service;

import com.hospitality.dto.admin.*;
import com.hospitality.enums.PayoutStatus;
import com.hospitality.enums.RefundStatus;
import com.hospitality.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public interface FinanceService {

    FinanceSummaryDto getFinanceSummary();
    List<RevenueTrendPointDto> getRevenueTrend();

    TransactionPageDto getAllTransactions(String search, TransactionType type, LocalDateTime from, LocalDateTime to, int page, int size);
    TransactionDto getTransactionById(Long id);

    RefundPageDto getAllRefunds(String search, RefundStatus status, int page, int size);
    RefundDto approveRefund(Long id);
    RefundDto rejectRefund(Long id, String adminNote);

    PayoutPageDto getAllPayouts(String search, PayoutStatus status, int page, int size);
    PayoutDto processPayout(Long id, String bankReference);
}
