ALTER TABLE staff.orders ADD COLUMN IF NOT EXISTS guest_instructions TEXT;
ALTER TABLE staff.orders ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
ALTER TABLE staff.orders ADD COLUMN IF NOT EXISTS staff_notes TEXT;

CREATE TABLE IF NOT EXISTS staff.item_reviews (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    guest_name VARCHAR(255),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
