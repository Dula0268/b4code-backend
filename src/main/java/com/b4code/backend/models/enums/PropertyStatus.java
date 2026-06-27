package com.b4code.backend.models.enums;

public enum PropertyStatus {
    // Admin approval workflow
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    // Owner operational states (set after APPROVED)
    ACTIVE,
    INACTIVE,
    MAINTENANCE
}
