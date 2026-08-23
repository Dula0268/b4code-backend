package com.b4code.backend.models.enums;

/**
 * Outcome of the refund attempt attached to a cancelled order.
 *
 * NOT_APPLICABLE - the order was never actually paid (e.g. cash / pay-at-property, or an
 *                  online order cancelled while still PAYMENT_PENDING), so nothing is owed.
 * REFUNDED       - a successful payment was found and reversed.
 * FAILED         - a paid payment was found but the refund could not be completed.
 */
public enum OrderRefundStatus {
    NOT_APPLICABLE,
    REFUNDED,
    FAILED
}
