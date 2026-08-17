package com.b4code.backend.rest;

import com.b4code.backend.dto.*;
import com.b4code.backend.models.enums.DisputeStatus;
import com.b4code.backend.models.enums.ModerationAction;
import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.models.enums.FlagType;
import com.b4code.backend.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/moderation")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin — Moderation", description = "Reviews queue, disputes hub, and moderation history")
public class ModerationController {

    private final ModerationService moderationService;
    private final com.b4code.backend.service.ModerationExportService moderationExportService;

    // ── GET tab badge counts (reviews + disputes)
    @GetMapping("/counts")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Get counts for moderation dashboard KPI badges")
    public ResponseEntity<Map<String, Object>> getBadgeCounts() {
        return ResponseEntity.ok(Map.of(
                "pendingReviews", moderationService.getPendingReviewCount(),
                "openDisputes",   moderationService.getOpenDisputeCount(),
                "removedToday",   moderationService.getRemovedTodayCount(),
                "resolvedDisputes", moderationService.getResolvedDisputesCount(),
                "totalResolvedAmount", moderationService.getTotalResolvedAmount()
        ));
    }

    // ── Reviews Queue
    @GetMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "List flagged reviews with optional status filter, flagType, rating and search")
    public ResponseEntity<Page<FlaggedReviewDto>> getFlaggedReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) FlagType flagType,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) page = 0;
        if (status == null) status = ReviewStatus.FLAGGED;
        return ResponseEntity.ok(moderationService.getFlaggedReviews(status, flagType, rating, search, page, size));
    }

    // PUT /api/admin/moderation/reviews/{id}/approve
    @PutMapping("/reviews/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve (keep) a flagged review")
    public ResponseEntity<FlaggedReviewDto> approveReview(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String adminNote = (body != null) ? body.getOrDefault("adminNote", "") : "";
        return ResponseEntity.ok(moderationService.approveReview(id, adminNote));
    }

    // PUT /api/admin/moderation/reviews/{id}/remove   
    @PutMapping("/reviews/{id}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a flagged review with an admin note")
    public ResponseEntity<FlaggedReviewDto> removeReview(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(moderationService.removeReview(id, body.getOrDefault("adminNote", "")));
    }

    // ── Disputes Hub
    @GetMapping("/disputes")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "List open disputes with optional status filter and search")
    public ResponseEntity<Page<DisputeDto>> getDisputes(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) page = 0;
        return ResponseEntity.ok(moderationService.getDisputes(status, search, page, size));
    }

    // PUT /api/admin/moderation/disputes/{id}/resolve
    @PutMapping("/disputes/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve a dispute — optionally approve refund")
    public ResponseEntity<DisputeDto> resolveDispute(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String resolution = (String) body.getOrDefault("resolution", "Resolved by admin");
        boolean refund = Boolean.TRUE.equals(body.get("refundApproved"));
        return ResponseEntity.ok(moderationService.resolveDispute(id, resolution, refund));
    }

    // PUT /api/admin/moderation/disputes/{id}/note
    @PutMapping("/disputes/{id}/note")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Save an internal admin note on a dispute")
    public ResponseEntity<DisputeDto> saveDisputeNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String note = body.getOrDefault("note", "");
        return ResponseEntity.ok(moderationService.saveDisputeNote(id, note));
    }

    // ── History tab
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Paginated moderation history with action and date-range filters")
    public ResponseEntity<Page<ModerationHistoryDto>> getHistory(
            @RequestParam(required = false) ModerationAction action,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        if (page < 0) page = 0;
        return ResponseEntity.ok(moderationService.getHistory(action, search, from, to, page, size));
    }

    // ── EXPORT Reviews
    @GetMapping(value = "/reviews/export/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Export review queue to CSV")
    public ResponseEntity<byte[]> exportReviewsCsv(
            @RequestParam(required = false) FlagType flagType,
            @RequestParam(required = false) Integer rating) {
        byte[] data = moderationExportService.exportReviewsToCsv(flagType, rating);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"review-queue.csv\"")
                .body(data);
    }

    @GetMapping(value = "/reviews/export/pdf", produces = "application/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Export review queue to PDF")
    public ResponseEntity<byte[]> exportReviewsPdf(
            @RequestParam(required = false) FlagType flagType,
            @RequestParam(required = false) Integer rating) {
        byte[] data = moderationExportService.exportReviewsToPdf(flagType, rating);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"review-queue.pdf\"")
                .body(data);
    }

    // ── EXPORT Disputes
    @GetMapping(value = "/disputes/export/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Export disputes to CSV")
    public ResponseEntity<byte[]> exportDisputesCsv(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) String search) {
        byte[] data = moderationExportService.exportDisputesToCsv(status, search);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"disputes.csv\"")
                .body(data);
    }

    @GetMapping(value = "/disputes/export/pdf", produces = "application/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Export disputes to PDF")
    public ResponseEntity<byte[]> exportDisputesPdf(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) String search) {
        byte[] data = moderationExportService.exportDisputesToPdf(status, search);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"disputes.pdf\"")
                .body(data);
    }
}
