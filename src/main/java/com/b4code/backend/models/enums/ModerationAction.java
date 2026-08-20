package com.b4code.backend.models.enums;

// Phase 6 — Moderation: history action taken
// Matches frontend HistoryAction type: "Review Removed" | "Refund Issued" | "Review Kept" | "Appeal Denied"
public enum ModerationAction {
    REVIEW_REMOVED,
    REFUND_ISSUED,
    REVIEW_KEPT,
    APPEAL_DENIED
}
