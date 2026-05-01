-- Add QR Code foreign key to Orders table
ALTER TABLE orders
ADD COLUMN qr_code_id BIGINT NULL;

ALTER TABLE orders
ADD CONSTRAINT fk_order_qr_code 
FOREIGN KEY (qr_code_id) REFERENCES qr_codes(id) ON DELETE SET NULL;

CREATE INDEX idx_qr_code_id ON orders(qr_code_id);
