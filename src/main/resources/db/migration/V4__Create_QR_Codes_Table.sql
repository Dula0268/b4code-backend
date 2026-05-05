CREATE TABLE IF NOT EXISTS qr_codes (
    id SERIAL PRIMARY KEY,
    unique_qr_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    instruction_text VARCHAR(255) DEFAULT 'Scan to Order',
    show_room_number BOOLEAN DEFAULT FALSE,
    show_logo BOOLEAN DEFAULT TRUE,
    property_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    scans INTEGER DEFAULT 0,
    last_scanned_at TIMESTAMP WITHOUT TIME ZONE,
    qr_image_url TEXT
);
