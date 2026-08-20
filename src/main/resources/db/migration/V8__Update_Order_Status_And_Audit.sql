-- Update existing order statuses to upper case enum formats
UPDATE staff.orders SET status = 'PLACED' WHERE status = 'placed' OR status = 'NEW' OR status IS NULL;
UPDATE staff.orders SET status = 'ACCEPTED' WHERE status = 'accepted';
UPDATE staff.orders SET status = 'IN_PROGRESS' WHERE status = 'in-progress' OR status = 'in_progress';
UPDATE staff.orders SET status = 'READY' WHERE status = 'ready';
UPDATE staff.orders SET status = 'DELIVERED' WHERE status = 'delivered';
UPDATE staff.orders SET status = 'CANCELLED' WHERE status = 'cancelled' OR status = 'rejected';

-- Add audit columns to orders table
ALTER TABLE staff.orders 
ADD COLUMN updated_at TIMESTAMP,
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255);

-- Create order_status_logs table for audit trail
CREATE TABLE staff.order_status_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_status_logs_order FOREIGN KEY (order_id) REFERENCES staff.orders(id) ON DELETE CASCADE
);
