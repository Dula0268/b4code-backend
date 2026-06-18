-- Update qr_codes table to match the new architecture
ALTER TABLE qr_codes DROP COLUMN IF EXISTS order_id;
ALTER TABLE qr_codes ADD COLUMN IF NOT EXISTS unique_qr_id VARCHAR(255);
ALTER TABLE qr_codes ADD COLUMN IF NOT EXISTS table_id BIGINT;
ALTER TABLE qr_codes ADD COLUMN IF NOT EXISTS room_number VARCHAR(100);

-- Standardize the QR Types to either 'ROOM' or 'TABLE'
UPDATE qr_codes SET type = 'TABLE' WHERE type IN ('Outdoor', 'Bar');

-- Update orders table in the staff schema to support tables and walk-ins
ALTER TABLE staff.orders ADD COLUMN IF NOT EXISTS table_id BIGINT;
ALTER TABLE staff.orders ADD COLUMN IF NOT EXISTS table_number VARCHAR(100);
ALTER TABLE staff.orders ADD COLUMN IF NOT EXISTS guest_name VARCHAR(255);
ALTER TABLE staff.orders ADD COLUMN IF NOT EXISTS guest_phone VARCHAR(50);

-- Allow guest_id to be nullable for walk-in orders
ALTER TABLE staff.orders ALTER COLUMN guest_id DROP NOT NULL;
