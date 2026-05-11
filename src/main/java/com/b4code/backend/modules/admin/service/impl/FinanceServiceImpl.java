package com.b4code.backend.modules.admin.service.impl;

import com.b4code.backend.modules.admin.dao.PayoutRepository;
import com.b4code.backend.modules.admin.dao.RefundRepository;
import com.b4code.backend.modules.admin.dao.TransactionRepository;
import com.b4code.backend.modules.admin.dto.*;
import com.b4code.backend.modules.admin.enums.PayoutStatus;
import com.b4code.backend.modules.admin.enums.RefundStatus;
import com.b4code.backend.modules.admin.enums.TransactionType;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.modules.admin.models.Payout;
import com.b4code.backend.modules.admin.models.Refund;
import com.b4code.backend.modules.admin.models.Transaction;
import com.b4code.backend.modules.admin.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final TransactionRepository transactionRepository;
    private final RefundRepository refundRepository;
    private final PayoutRepository payoutRepository;

    // ── Finance summary KPIs
    @Override
    @Transactional(readOnly = true)
    public FinanceSummaryDto getFinanceSummary() {
        BigDecimal approvedRefunds = refundRepository.sumApprovedRefunds();
        return FinanceSummaryDto.builder()
                .totalRevenue(transactionRepository.sumTotalRevenue())
                .platformCommission(transactionRepository.sumPlatformCommission())
                .totalPayouts(payoutRepository.sumProcessedPayouts())
                .totalRefunds(approvedRefunds)
                .pendingRefunds(approvedRefunds)   // frontend reads pendingRefunds
                .currency("LKR")
                .build();
    }

    // ── Revenue trend chart (monthly)
    @Override
    @Transactional(readOnly = true)
    public List<RevenueTrendPointDto> getRevenueTrend() {
        return transactionRepository.getMonthlyRevenueTrend()
                .stream()
                .map(row -> RevenueTrendPointDto.builder()
                        .month((String) row[0])
                        .revenue((BigDecimal) row[1])
                        .build())
                .toList();
    }

    // ── Transactions: paginated list
    @Override
    @Transactional(readOnly = true)
    public TransactionPageDto getAllTransactions(String search, TransactionType type, LocalDateTime from, LocalDateTime to, int page, int size) {
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        Page<Transaction> pageResult =
                transactionRepository.findAllWithFilters(
                        searchTerm, type, from, to,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                );
        return TransactionPageDto.builder()
                .content(pageResult.map(TransactionDto::fromEntity).toList())
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .build();
    }

    // ── Transactions: single
    @Override
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        return TransactionDto.fromEntity(
                transactionRepository.findById(id)
                        .orElseThrow(() -> new CustomException("Transaction id=" + id + " not found.", HttpStatus.NOT_FOUND))
        );
    }

    // ── Refunds: paginated list
    @Override
    @Transactional(readOnly = true)
    public RefundPageDto getAllRefunds(String search, RefundStatus status, int page, int size) {
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        Page<Refund> pageResult = refundRepository.findAllWithFilters(
                status, searchTerm,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"))
        );
        return RefundPageDto.builder()
                .content(pageResult.map(RefundDto::fromEntity).toList())
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .build();
    }

    // ── Refunds: approve
    @Override
    @Transactional
    public RefundDto approveRefund(Long id) {
        Refund refund = findRefundOrThrow(id);
        refund.setStatus(RefundStatus.APPROVED);
        log.info("Refund id={} APPROVED", id);
        return RefundDto.fromEntity(refundRepository.save(refund));
    }

    // ── Refunds: reject
    @Override
    @Transactional
    public RefundDto rejectRefund(Long id, String adminNote) {
        Refund refund = findRefundOrThrow(id);
        refund.setStatus(RefundStatus.REJECTED);
        refund.setAdminNote(adminNote);
        log.info("Refund id={} REJECTED", id);
        return RefundDto.fromEntity(refundRepository.save(refund));
    }

    // ── Payouts: paginated list
    @Override
    @Transactional(readOnly = true)
    public PayoutPageDto getAllPayouts(String search, PayoutStatus status, int page, int size) {
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        Page<Payout> pageResult = payoutRepository.findAllWithFilters(
                status, searchTerm,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"))
        );
        return PayoutPageDto.builder()
                .content(pageResult.map(PayoutDto::fromEntity).toList())
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .build();
    }

    // ── Payouts: process
    @Override
    @Transactional
    public PayoutDto processPayout(Long id, String bankReference) {
        Payout payout = payoutRepository.findById(id)
                .orElseThrow(() -> new CustomException("Payout id=" + id + " not found.", HttpStatus.NOT_FOUND));
        payout.setStatus(PayoutStatus.PROCESSED);
        payout.setBankReference(bankReference);
        log.info("Payout id={} PROCESSED — ref='{}'", id, bankReference);
        return PayoutDto.fromEntity(payoutRepository.save(payout));
    }

    // ── Private helpers
    private Refund findRefundOrThrow(Long id) {
        return refundRepository.findById(id)
                .orElseThrow(() -> new CustomException("Refund id=" + id + " not found.", HttpStatus.NOT_FOUND));
    }
}
