CREATE TABLE IF NOT EXISTS staff.order_messages (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES staff.orders(id) ON DELETE CASCADE,
    sender_identifier VARCHAR(255),
    sender_role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
