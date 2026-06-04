package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PayoutRepository;
import com.b4code.backend.dao.PlatformConfigRepository;
import com.b4code.backend.dao.RefundRepository;
import com.b4code.backend.dao.TransactionRepository;
import com.b4code.backend.dto.*;
import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.models.enums.RefundStatus;
import com.b4code.backend.models.enums.TransactionType;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.PlatformConfig;
import com.b4code.backend.models.Payout;
import com.b4code.backend.models.Refund;
import com.b4code.backend.models.Transaction;
import com.b4code.backend.service.FinanceService;
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
        private final PlatformConfigRepository platformConfigRepository;

        private static final String COMMISSION_KEY = "COMMISSION_RATE";
        private static final BigDecimal DEFAULT_COMMISSION = new BigDecimal("20.00");

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
                                .pendingRefunds(approvedRefunds) // frontend reads pendingRefunds
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
        public TransactionPageDto getAllTransactions(String search, TransactionType type, LocalDateTime from,
                        LocalDateTime to, int page, int size) {
                String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
                Page<Transaction> pageResult = transactionRepository.findAllWithFilters(
                                searchTerm, type, from, to,
                                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
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
                                                .orElseThrow(() -> new CustomException(
                                                                "Transaction id=" + id + " not found.",
                                                                HttpStatus.NOT_FOUND)));
        }

        // ── Refunds: paginated list
        @Override
        @Transactional(readOnly = true)
        public RefundPageDto getAllRefunds(String search, RefundStatus status, int page, int size) {
                String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
                Page<Refund> pageResult = refundRepository.findAllWithFilters(
                                status, searchTerm,
                                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt")));
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
                                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt")));
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
        public PayoutDto processPayout(Long id, String bankReference, BigDecimal commissionRate) {
                Payout payout = payoutRepository.findById(id)
                                .orElseThrow(() -> new CustomException("Payout id=" + id + " not found.",
                                                HttpStatus.NOT_FOUND));

                if (commissionRate != null && payout.getHotelAmount() != null) {
                        payout.setCommissionRate(commissionRate);
                        BigDecimal commission = payout.getHotelAmount()
                                        .multiply(commissionRate)
                                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        payout.setCommissionAmount(commission);

                        BigDecimal foodAmt = payout.getFoodAmount() != null ? payout.getFoodAmount() : BigDecimal.ZERO;
                        payout.setAmount(payout.getHotelAmount().subtract(commission).add(foodAmt));
                }

                payout.setStatus(PayoutStatus.PROCESSED);
                payout.setBankReference(bankReference);
                log.info("Payout id={} PROCESSED — ref='{}'", id, bankReference);
                return PayoutDto.fromEntity(payoutRepository.save(payout));
        }

        @Override
        @Transactional
        public PayoutDto rejectPayout(Long id) {
                Payout payout = payoutRepository.findById(id)
                                .orElseThrow(() -> new CustomException("Payout id=" + id + " not found.",
                                                HttpStatus.NOT_FOUND));

                payout.setStatus(PayoutStatus.REJECTED);
                log.info("Payout id={} REJECTED", id);
                return PayoutDto.fromEntity(payoutRepository.save(payout));
        }

        // ── Private helpers
        private Refund findRefundOrThrow(Long id) {
                return refundRepository.findById(id)
                                .orElseThrow(() -> new CustomException("Refund id=" + id + " not found.",
                                                HttpStatus.NOT_FOUND));
        }

        // ── Commission rate configuration
        @Override
        @Transactional(readOnly = true)
        public BigDecimal getCommissionRate() {
                return platformConfigRepository.findByConfigKey(COMMISSION_KEY)
                                .map(c -> new BigDecimal(c.getConfigValue()))
                                .orElse(DEFAULT_COMMISSION);
        }

        @Override
        @Transactional
        public BigDecimal updateCommissionRate(BigDecimal newRate) {
                if (newRate == null || newRate.compareTo(BigDecimal.ZERO) < 0
                                || newRate.compareTo(new BigDecimal("100")) > 0) {
                        throw new CustomException("Commission rate must be between 0 and 100.", HttpStatus.BAD_REQUEST);
                }
                PlatformConfig config = platformConfigRepository.findByConfigKey(COMMISSION_KEY)
                                .orElseGet(() -> PlatformConfig.builder()
                                                .configKey(COMMISSION_KEY)
                                                .description("Platform commission rate applied to hotel booking revenue (%)")
                                                .build());
                config.setConfigValue(newRate.toPlainString());
                platformConfigRepository.save(config);
                log.info("Commission rate updated to {}%", newRate);
                return newRate;
        }
}
