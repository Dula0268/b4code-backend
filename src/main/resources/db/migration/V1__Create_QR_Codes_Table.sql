-- Create QR Codes Table
CREATE TABLE IF NOT EXISTS qr_codes (
    id BIGSERIAL PRIMARY KEY,
    unique_qr_id VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    type VARCHAR(50) NOT NULL DEFAULT 'DINING_TABLE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    instruction_text VARCHAR(255),
    show_room_number BOOLEAN DEFAULT FALSE,
    show_logo BOOLEAN DEFAULT TRUE,
    property_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    qr_image BYTEA,
    scans INT DEFAULT 0,
    last_scanned_at TIMESTAMP NULL,
    CONSTRAINT fk_qr_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    CONSTRAINT fk_qr_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_qr_property_id ON qr_codes(property_id);
CREATE INDEX idx_qr_unique_qr_id ON qr_codes(unique_qr_id);
CREATE INDEX idx_qr_status ON qr_codes(status);
CREATE INDEX idx_qr_created_at ON qr_codes(created_at);
