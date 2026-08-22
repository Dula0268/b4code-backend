-- Cancellation attribution + refund outcome on food orders.
-- Lets the guest/staff screens distinguish "cancelled by guest" from "cancelled by staff"
-- and show whether the guest's money was actually returned.

ALTER TABLE staff.orders
    ADD COLUMN IF NOT EXISTS cancelled_by      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS cancelled_at      TIMESTAMP,
    ADD COLUMN IF NOT EXISTS refund_status     VARCHAR(20),
    ADD COLUMN IF NOT EXISTS refund_amount     DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS refund_reference  VARCHAR(100),
    ADD COLUMN IF NOT EXISTS refunded_at       TIMESTAMP;

-- Explicit actor type on the audit trail: changed_by holds a guest display name for guest
-- actions and a staff email for staff actions, which is not machine-readable on its own.
ALTER TABLE staff.order_status_logs
    ADD COLUMN IF NOT EXISTS changed_by_role VARCHAR(20);

-- Backfill: existing CANCELLED orders predate the actor field. They were cancellable from
-- either side with no record of which, so leave cancelled_by NULL (the UI falls back to a
-- neutral "Cancelled") and mark the refund state as unknown-but-not-issued.
UPDATE staff.orders
SET refund_status = 'NOT_APPLICABLE'
WHERE status = 'CANCELLED' AND refund_status IS NULL;
