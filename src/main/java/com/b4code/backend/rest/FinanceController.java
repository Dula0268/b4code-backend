package com.b4code.backend.rest;

import com.b4code.backend.dto.*;
import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.models.enums.RefundStatus;
import com.b4code.backend.models.enums.TransactionType;
import com.b4code.backend.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/finance")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Admin — Finance", description = "Manage transactions, refunds and payouts")
public class FinanceController {
    private final FinanceService financeService;
    private final com.b4code.backend.service.FinanceExportService financeExportService;

    // ── GET finance summary KPIs
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Finance KPI summary")
    public ResponseEntity<FinanceSummaryDto> getSummary() {
        return ResponseEntity.ok(financeService.getFinanceSummary());
    }

    // ── GET revenue trend chart data
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Monthly revenue trend for chart")
    public ResponseEntity<List<RevenueTrendPointDto>> getRevenueTrend() {
        return ResponseEntity.ok(financeService.getRevenueTrend());
    }

    // ── GET all transactions (paginated)
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List all transactions with filters")
    public ResponseEntity<TransactionPageDto> getAllTransactions(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0)
            page = 0;
        return ResponseEntity.ok(financeService.getAllTransactions(search, type, from, to, page, size));
    }

    // ── GET single transaction
    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get transaction detail by ID")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getTransactionById(id));
    }

    // ── GET all refunds (paginated)
    @GetMapping("/refunds")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List refund requests")
    public ResponseEntity<RefundPageDto> getAllRefunds(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0)
            page = 0;
        return ResponseEntity.ok(financeService.getAllRefunds(search, status, page, size));
    }

    // ── APPROVE refund
    @PutMapping("/refunds/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Approve a refund request")
    public ResponseEntity<RefundDto> approveRefund(@PathVariable Long id) {
        log.info("PUT /api/admin/finance/refunds/{}/approve", id);
        return ResponseEntity.ok(financeService.approveRefund(id));
    }

    // ── REJECT refund
    @PutMapping("/refunds/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Reject a refund request")
    public ResponseEntity<RefundDto> rejectRefund(
            @PathVariable Long id,
            @RequestBody RefundRejectRequest request) {
        log.info("PUT /api/admin/finance/refunds/{}/reject", id);
        return ResponseEntity.ok(financeService.rejectRefund(id, request.adminNote()));
    }

    // ── GET all payouts (paginated)
    @GetMapping("/payouts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List payout requests")
    public ResponseEntity<PayoutPageDto> getAllPayouts(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0)
            page = 0;
        return ResponseEntity.ok(financeService.getAllPayouts(search, status, page, size));
    }

    // ── EXPORT payouts
    @GetMapping(value = "/payouts/export/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Export payout requests to CSV")
    public ResponseEntity<byte[]> exportPayoutsCsv(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) PayoutStatus status) {
        byte[] data = financeExportService.exportPayoutsToCsv(status, search);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"payouts.csv\"")
                .body(data);
    }

    @GetMapping(value = "/payouts/export/pdf", produces = "application/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Export payout requests to PDF")
    public ResponseEntity<byte[]> exportPayoutsPdf(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) PayoutStatus status) {
        byte[] data = financeExportService.exportPayoutsToPdf(status, search);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"payouts.pdf\"")
                .body(data);
    }

    // ── PROCESS payout
    @PutMapping("/payouts/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Mark a payout as processed")
    public ResponseEntity<PayoutDto> processPayout(
            @PathVariable Long id,
            @RequestBody PayoutProcessRequest request) {
        log.info("PUT /api/admin/finance/payouts/{}/process — ref='{}'", id, request.bankReference());
        return ResponseEntity.ok(financeService.processPayout(id, request.bankReference(), request.commissionRate()));
    }

    // ── REJECT payout
    @PutMapping("/payouts/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Reject a payout")
    public ResponseEntity<PayoutDto> rejectPayout(@PathVariable Long id) {
        log.info("PUT /api/admin/finance/payouts/{}/reject", id);
        return ResponseEntity.ok(financeService.rejectPayout(id));
    }

    // ── GET commission rate
    @GetMapping("/commission")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get current platform commission rate")
    public ResponseEntity<Map<String, Object>> getCommissionRate() {
        BigDecimal rate = financeService.getCommissionRate();
        return ResponseEntity.ok(Map.of(
                "commissionRate", rate,
                "description", "Percentage applied to hotel booking revenue only. Food orders are commission-free."));
    }

    // ── UPDATE commission rate
    @PutMapping("/commission")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update platform commission rate")
    public ResponseEntity<Map<String, Object>> updateCommissionRate(
            @RequestBody CommissionUpdateRequest request) {
        log.info("PUT /api/admin/finance/commission — newRate={}", request.commissionRate());
        BigDecimal updated = financeService.updateCommissionRate(request.commissionRate());
        return ResponseEntity.ok(Map.of(
                "commissionRate", updated,
                "message", "Commission rate updated successfully."));
    }

    // ── Request body records
    public record RefundRejectRequest(String adminNote) {
    }

    public record PayoutProcessRequest(String bankReference, BigDecimal commissionRate) {
    }

    public record CommissionUpdateRequest(BigDecimal commissionRate) {
    }
}
