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
import com.b4code.backend.models.User;
import com.b4code.backend.service.FinanceService;
import com.b4code.backend.service.NotificationService;
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
        private final com.b4code.backend.dao.BankAccountRepository bankAccountRepository;
        private final com.b4code.backend.dao.UserRepository userRepository;
        private final NotificationService notificationService;
        private final com.b4code.backend.dao.BookingRepository bookingRepository;

        private static final String COMMISSION_KEY = "COMMISSION_RATE";
        private static final BigDecimal DEFAULT_COMMISSION = new BigDecimal("20.00");

        // ── Finance summary KPIs
        @Override
        @Transactional(readOnly = true)
        public FinanceSummaryDto getFinanceSummary() {
                BigDecimal approvedRefunds = refundRepository.sumApprovedRefunds();
                return FinanceSummaryDto.builder()
                                .totalRevenue(transactionRepository.sumTotalRevenue())
                                .revenueGrowth("+12.5%")
                                .platformCommission(transactionRepository.sumPlatformCommission())
                                .commissionGrowth("+8.2%")
                                .totalPayouts(payoutRepository.sumProcessedPayouts())
                                .payoutGrowth("+15.0%")
                                .totalRefunds(approvedRefunds)
                                .refundsGrowth("-2.4%")
                                .pendingRefunds(approvedRefunds) // frontend reads pendingRefunds
                                .currency("LKR")
                                .build();
        }

        // ── Revenue trend chart (dynamic)
        @Override
        @Transactional(readOnly = true)
        public List<RevenueTrendPointDto> getRevenueTrend(String timeframe) {
                List<Object[]> rawData;
                java.util.Map<String, BigDecimal> dataMap = new java.util.LinkedHashMap<>();

                if ("today".equalsIgnoreCase(timeframe)) {
                        // Initialize hours 00:00 to 23:00
                        for (int i = 0; i < 24; i++) {
                                dataMap.put(String.format("%02d:00", i), BigDecimal.ZERO);
                        }
                        rawData = bookingRepository.getTodayBookingRevenueTrend();
                } else if ("7days".equalsIgnoreCase(timeframe)) {
                        // Initialize last 7 days (mock with general days for simplicity, but backend
                        // gets actual days)
                        java.time.LocalDate today = java.time.LocalDate.now();
                        for (int i = 6; i >= 0; i--) {
                                String dayStr = today.minusDays(i)
                                                .format(java.time.format.DateTimeFormatter.ofPattern("EEE"));
                                dataMap.put(dayStr, BigDecimal.ZERO);
                        }
                        rawData = bookingRepository.getWeeklyBookingRevenueTrend();
                } else {
                        // Initialize months Jan-Dec
                        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
                                        "Dec" };
                        for (String m : months) {
                                dataMap.put(m, BigDecimal.ZERO);
                        }
                        rawData = bookingRepository.getMonthlyBookingRevenueTrend();
                }

                // Populate with actual data
                for (Object[] row : rawData) {
                        String label = (String) row[0];
                        BigDecimal val = (BigDecimal) row[1];
                        // Clean label if necessary (e.g. Postgres might return padded string)
                        if (label != null) {
                                dataMap.put(label.trim(), val);
                        }
                }

                BigDecimal commissionRate = getCommissionRate().divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);

                return dataMap.entrySet().stream()
                                .map(entry -> RevenueTrendPointDto.builder()
                                                .month(entry.getKey())
                                                .revenue(entry.getValue())
                                                .netRevenue(entry.getValue().multiply(commissionRate))
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
                                .content(pageResult.map(this::mapToPayoutDto).toList())
                                .currentPage(pageResult.getNumber())
                                .totalPages(pageResult.getTotalPages())
                                .totalElements(pageResult.getTotalElements())
                                .pageSize(pageResult.getSize())
                                .build();
        }

        // ── Payouts: export
        @Override
        @Transactional(readOnly = true)
        public void exportPayoutsToCsv(String search, PayoutStatus status,
                        jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
                String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
                Page<Payout> pageResult = payoutRepository.findAllWithFilters(
                                status, searchTerm,
                                org.springframework.data.domain.Pageable.unpaged());

                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=\"payout-report.csv\"");

                try (java.io.PrintWriter writer = response.getWriter()) {
                        writer.println("Payout ID,Property Name,Owner Name,Status,Currency,Gross Amount,Platform Commission,Net Payout,Bank Reference,Requested Date,Processed Date");
                        for (Payout p : pageResult.getContent()) {
                                BigDecimal gross = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
                                BigDecimal commission = p.getCommissionAmount() != null ? p.getCommissionAmount()
                                                : BigDecimal.ZERO;
                                BigDecimal net = gross.subtract(commission); // Alternatively, if amount is net:
                                                                             // p.getAmount() and gross = p.getAmount()
                                                                             // + p.getCommissionAmount()
                                // Actually, in processPayout:
                                // payout.setAmount(payout.getHotelAmount().subtract(commission).add(foodAmt));
                                // So amount is the net payout!
                                net = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
                                gross = net.add(commission);

                                String bankRef = p.getBankReference() != null ? p.getBankReference() : "";
                                if (bankRef.length() > 4) {
                                        bankRef = "***" + bankRef.substring(bankRef.length() - 4);
                                }

                                String requestedDate = p.getRequestedAt() != null ? p.getRequestedAt().toString() : "";
                                String processedDate = p.getProcessedAt() != null ? p.getProcessedAt().toString() : "";

                                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                                                p.getId(),
                                                p.getPropertyName() != null ? p.getPropertyName().replace("\"", "\"\"")
                                                                : "",
                                                p.getOwnerName() != null ? p.getOwnerName().replace("\"", "\"\"") : "",
                                                p.getStatus(),
                                                p.getCurrency(),
                                                gross,
                                                commission,
                                                net,
                                                bankRef,
                                                requestedDate,
                                                processedDate);
                        }
                }
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
                Payout saved = payoutRepository.save(payout);
                User owner = userRepository.findById(saved.getOwnerId()).orElse(null);
                if (owner != null) {
                        notificationService.createNotification(
                                owner,
                                "Payout approved",
                                "Your payout request for " + saved.getPropertyName() + " has been processed successfully."
                        );
                }
                log.info("Payout id={} PROCESSED — ref='{}'", id, bankReference);
                return mapToPayoutDto(saved);
        }

        @Override
        @Transactional
        public PayoutDto rejectPayout(Long id) {
                Payout payout = payoutRepository.findById(id)
                                .orElseThrow(() -> new CustomException("Payout id=" + id + " not found.",
                                                HttpStatus.NOT_FOUND));

                payout.setStatus(PayoutStatus.REJECTED);
                Payout saved = payoutRepository.save(payout);
                User owner = userRepository.findById(saved.getOwnerId()).orElse(null);
                if (owner != null) {
                        notificationService.createNotification(
                                owner,
                                "Payout rejected",
                                "Your payout request for " + saved.getPropertyName() + " was rejected by the admin. Please review your bank details or contact support."
                        );
                }
                log.info("Payout id={} REJECTED", id);
                return mapToPayoutDto(saved);
        }

        public PayoutDto mapToPayoutDto(Payout p) {
                PayoutDto dto = PayoutDto.fromEntity(p);
                if (p.getOwnerId() != null) {
                        List<com.b4code.backend.models.BankAccount> accounts = bankAccountRepository.findByOwnerIdOrderByIsPrimaryDescCreatedAtDesc(p.getOwnerId());
                        if (accounts != null && !accounts.isEmpty()) {
                                com.b4code.backend.models.BankAccount primary = accounts.get(0);
                                dto.setBankName(primary.getBankName());
                                dto.setAccountHolder(primary.getAccountHolder());
                                dto.setAccountNumber(primary.getAccountNumber());
                                dto.setBranchCode(primary.getBranchCode());
                                dto.setBankDetails(primary.getBankName() + " — Acc: " + primary.getAccountNumber() + " (Holder: " + primary.getAccountHolder() + ")");
                        } else {
                                dto.setBankDetails("No Bank Account Linked");
                        }
                } else {
                        dto.setBankDetails("No Owner Linked");
                }
                return dto;
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
