package com.b4code.backend.modules.admin.rest;

import com.b4code.backend.modules.admin.dto.*;
import com.b4code.backend.modules.admin.enums.DisputeStatus;
import com.b4code.backend.modules.admin.enums.ModerationAction;
import com.b4code.backend.modules.admin.enums.ReviewStatus;
import com.b4code.backend.modules.admin.service.ModerationService;
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

    // ── GET tab badge counts (reviews + disputes)
    @GetMapping("/counts")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Badge counts for Reviews Queue and Disputes tabs")
    public ResponseEntity<Map<String, Long>> getBadgeCounts() {
        return ResponseEntity.ok(Map.of(
                "pendingReviews", moderationService.getPendingReviewCount(),
                "openDisputes",   moderationService.getOpenDisputeCount()
        ));
    }

    // ── Reviews Queue
    @GetMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "List flagged reviews with optional status filter and search")
    public ResponseEntity<Page<FlaggedReviewDto>> getFlaggedReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(moderationService.getFlaggedReviews(status, search, page, size));
    }

    // PUT /api/admin/moderation/reviews/{id}/approve
    @PutMapping("/reviews/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve (keep) a flagged review")
    public ResponseEntity<FlaggedReviewDto> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(moderationService.approveReview(id));
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
        return ResponseEntity.ok(moderationService.getHistory(action, search, from, to, page, size));
    }
}
