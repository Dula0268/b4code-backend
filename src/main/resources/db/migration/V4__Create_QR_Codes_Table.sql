-- Drop old table if exists (contains mismatched schema)
DROP TABLE IF EXISTS qr_codes CASCADE;

-- Create QR Codes table with proper schema
CREATE TABLE IF NOT EXISTS qr_codes (
    id BIGSERIAL PRIMARY KEY,
    qr_code_value VARCHAR(255) NOT NULL UNIQUE,
    order_id BIGINT,
    property_id BIGINT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    name VARCHAR(255),
    location VARCHAR(255),
    type VARCHAR(100),
    qr_image_data TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    scanned_at TIMESTAMP,
    description VARCHAR(500),
    instruction_text TEXT,
    show_room_number BOOLEAN DEFAULT FALSE,
    show_logo BOOLEAN DEFAULT TRUE
);
