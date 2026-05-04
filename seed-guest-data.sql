-- Seed test data for guest features

-- Insert test users first (if they don't exist)
INSERT INTO users (id, first_name, last_name, email, phone, password_hash, role, status, created_at, updated_at)
VALUES 
  (101, 'Kamal', 'Silva', 'kamal@example.com', '0712345671', 'hash1', 'GUEST', 'ACTIVE', NOW(), NOW()),
  (102, 'Nimal', 'Kumar', 'nimal@example.com', '0712345672', 'hash2', 'GUEST', 'ACTIVE', NOW(), NOW()),
  (103, 'Ravi', 'Sharma', 'ravi@example.com', '0712345673', 'hash3', 'OWNER', 'ACTIVE', NOW(), NOW()),
  (104, 'Priya', 'Patel', 'priya@example.com', '0712345674', 'hash4', 'OWNER', 'ACTIVE', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert test properties
INSERT INTO properties (id, name, owner_id, owner_name, pv_id, image_url, status, submitted_at, updated_at)
VALUES 
  (1, 'Luxury Beach Villa - Mirissa', 103, 'Ravi Sharma', 1, 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=500', 'APPROVED', NOW(), NOW()),
  (2, 'Mountain Escape - Nuwara Eliya', 103, 'Ravi Sharma', 2, 'https://images.unsplash.com/photo-1571896349842-b08d4ca884db?w=500', 'APPROVED', NOW(), NOW()),
  (3, 'City Center Apartment - Colombo', 104, 'Priya Patel', 3, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=500', 'APPROVED', NOW(), NOW()),
  (4, 'Tea Garden Cottage - Kandy', 103, 'Ravi Sharma', 4, 'https://images.unsplash.com/photo-1420550884002-271df01f8822?w=500', 'APPROVED', NOW(), NOW()),
  (5, 'Beachfront Resort - Galle', 104, 'Priya Patel', 5, 'https://images.unsplash.com/photo-1470252649378-9c29740ff023?w=500', 'APPROVED', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert test reviews
INSERT INTO reviews (id, property_id, guest_id, rating, review_text, created_at)
VALUES 
  (1, 1, 101, 5, 'Amazing villa with breathtaking ocean views. The staff was incredibly helpful!', NOW()),
  (2, 1, 102, 4, 'Beautiful place, slightly noisy at night due to beach bars nearby.', NOW()),
  (3, 2, 101, 5, 'Perfect mountain getaway. Very peaceful and romantic.', NOW()),
  (4, 3, 102, 3, 'Good location but rooms could be cleaner.', NOW()),
  (5, 4, 101, 5, 'Wonderful experience in the tea gardens. Must visit!', NOW()),
  (6, 5, 102, 4, 'Great beachfront property with excellent service.', NOW())
ON CONFLICT DO NOTHING;

-- Insert test messages
INSERT INTO messages (id, property_id, sender_id, receiver_id, content, sent_at)
VALUES 
  (1, 1, 101, 103, 'Hi! I am interested in booking your beach villa for next month. Is it available?', NOW()),
  (2, 1, 103, 101, 'Hello! Yes, we have availability. Would love to help you with the booking.', NOW()),
  (3, 1, 101, 103, 'Great! What are the rates for the month of June?', NOW()),
  (4, 2, 102, 103, 'Can I get more information about the tea garden cottage?', NOW()),
  (5, 3, 101, 104, 'Is this apartment suitable for families with kids?', NOW()),
  (6, 4, 102, 103, 'Are there any hiking trails nearby the cottage?', NOW()),
  (7, 5, 101, 104, 'Can you arrange airport pickup for us?', NOW()),
  (8, 1, 103, 102, 'Thanks for your interest! How many nights would you like to stay?', NOW())
ON CONFLICT DO NOTHING;

-- Insert test bookings (optional)
INSERT INTO bookings (id, property_id, guest_id, check_in_date, check_out_date, total_price, status, created_at)
VALUES 
  (1, 1, 101, '2026-06-01', '2026-06-07', 35000, 'CONFIRMED', NOW()),
  (2, 2, 102, '2026-06-15', '2026-06-20', 28000, 'CONFIRMED', NOW()),
  (3, 4, 101, '2026-07-01', '2026-07-05', 15000, 'PENDING', NOW())
ON CONFLICT DO NOTHING;

-- Conditionally insert rooms if a `rooms` table exists in this database.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'rooms') THEN
        -- Insert two sample rooms for property 1
        INSERT INTO rooms (id, property_id, name, max_guests, bed_type, sqft, price_per_night, original_price, tag, image_url, created_at)
        VALUES
          ('1-r1', 1, 'Master Suite', 4, 'King Bed', 450, 8000.0, 10000.0, 'Popular', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=500', NOW()),
          ('1-r2', 1, 'Family Room', 6, '2 x Queen', 600, 12000.0, 15000.0, 'Refundable', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=500', NOW())
        ON CONFLICT DO NOTHING;

        -- Insert two sample rooms for property 2
        INSERT INTO rooms (id, property_id, name, max_guests, bed_type, sqft, price_per_night, original_price, tag, image_url, created_at)
        VALUES
          ('2-r1', 2, 'Hillside Suite', 3, 'Queen Bed', 380, 7000.0, 9000.0, 'Popular', 'https://images.unsplash.com/photo-1571896349842-b08d4ca884db?w=500', NOW()),
          ('2-r2', 2, 'Couple Room', 2, 'Double Bed', 280, 5000.0, 6000.0, 'Refundable', 'https://images.unsplash.com/photo-1571896349842-b08d4ca884db?w=500', NOW())
        ON CONFLICT DO NOTHING;
    END IF;
END$$;

SELECT 'Seed data inserted successfully!' AS status;
