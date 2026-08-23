package com.b4code.backend.models.enums;

/**
 * Who performed an order state change. Persisted on {@link com.b4code.backend.models.Order}
 * (for cancellations) and on every {@link com.b4code.backend.models.OrderStatusLog} row, so
 * the guest-facing and staff-facing screens can say "cancelled by guest" vs
 * "cancelled by staff" instead of guessing from a free-text actor string.
 */
public enum OrderActorType {
    GUEST,
    STAFF,
    SYSTEM
}
