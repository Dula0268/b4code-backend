-- Clean up existing mock data to prevent duplicates
DELETE FROM staff.item_reviews;
DELETE FROM staff.order_items;
DELETE FROM staff.orders;
DELETE FROM staff.menu_item_modifier_options;
DELETE FROM staff.menu_item_modifiers;
DELETE FROM staff.menu_item_variants;
DELETE FROM staff.menu_item_images;
DELETE FROM staff.menu_items;
DELETE FROM staff.menu_categories;
DELETE FROM staff.menus;

-- Restart sequences 
ALTER SEQUENCE IF EXISTS staff.menus_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS staff.menu_categories_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS staff.menu_items_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS staff.menu_item_modifiers_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS staff.item_reviews_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS staff.orders_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS staff.order_items_id_seq RESTART WITH 1;

-- 1. Insert 2 Menus
INSERT INTO staff.menus (id, property_id, name, description, status) VALUES
(1, 1, 'In-Room Dining', 'Delicious meals delivered straight to your room', 'active'),
(2, 1, 'Poolside Bar', 'Refreshing drinks and light snacks by the pool', 'active');

-- 2. Insert 5 Categories
INSERT INTO staff.menu_categories (id, property_id, name) VALUES
(1, 1, 'Appetizers'),
(2, 1, 'Main Courses'),
(3, 1, 'Desserts'),
(4, 1, 'Beverages'),
(5, 1, 'Healthy Options');

-- 3. Insert 8 Menu Items
INSERT INTO staff.menu_items (id, property_id, menu_id, category_id, name, description, price, is_available, tag, calories) VALUES
-- Menu 1 (In-Room Dining)
(1, 1, 1, 1, 'Crispy Calamari', 'Lightly breaded calamari served with tartare sauce', 1250, true, 'Popular', 450),
(2, 1, 1, 2, 'Grilled Wagyu Burger', 'Premium wagyu beef patty with truffle mayo and fries', 2400, true, 'Chef Special', 950),
(3, 1, 1, 3, 'New York Cheesecake', 'Classic creamy cheesecake with berry compote', 900, true, 'Sweet', 600),
(4, 1, 1, 4, 'Fresh Orange Juice', 'Freshly squeezed oranges', 600, true, 'Vegan', 120),
(5, 1, 1, 2, 'Margherita Pizza', 'Wood-fired pizza with fresh mozzarella and basil', 1600, true, 'Vegetarian', 800),

-- Menu 2 (Poolside Bar)
(6, 1, 2, 1, 'Nachos Supreme', 'Tortilla chips topped with cheese, jalapenos, and guac', 1400, true, 'Spicy', 700),
(7, 1, 2, 2, 'Club Sandwich', 'Triple-decker sandwich with turkey, bacon, and fries', 1800, true, 'Classic', 850),
(8, 1, 2, 4, 'Mojito', 'Classic rum cocktail with fresh mint and lime', 1200, true, 'Alcoholic', 200);

-- 4. Insert Images for the Items
INSERT INTO staff.menu_item_images (menu_item_id, image_url) VALUES
-- Item 1: Crispy Calamari
(1, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564443/converted_image_1_xfvfaq.jpg'),
(1, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564442/converted_image_ppo4vi.jpg'),
(1, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564441/converted_image_2_g6wesk.jpg'),

-- Item 2: Grilled Wagyu Burger
(2, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564664/converted_image_3_auib5g.jpg'),
(2, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564663/converted_image_5_n7hsv7.jpg'),
(2, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564663/converted_image_4_q1ii0z.jpg'),

-- Item 3: New York Cheesecake
(3, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564801/converted_image_6_s3onyq.jpg'),
(3, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564802/converted_image_7_a2pcch.jpg'),

-- Item 4: Fresh Orange Juice
(4, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564882/converted_image_10_wqskis.jpg'),
(4, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564882/converted_image_9_ihpuel.jpg'),
(4, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564881/converted_image_8_oezom4.jpg'),

-- Item 5: Margherita Pizza
(5, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564966/converted_image_11_sabusd.jpg'),
(5, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564967/converted_image_12_pjhlwz.jpg'),

-- Item 6: Nachos Supreme
(6, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782564968/converted_image_13_u22hby.jpg'),
(6, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782565100/converted_image_14_wqcsas.jpg'),
(6, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782565102/converted_image_15_uigaz6.jpg'),

-- Item 7: Club Sandwich
(7, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782565155/converted_image_16_brp8ch.jpg'),
(7, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782565156/converted_image_17_l4feqm.jpg'),

-- Item 8: Mojito
(8, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782565316/converted_image_19_pe5pus.jpg'),
(8, 'https://res.cloudinary.com/dh9ige5on/image/upload/v1782565315/converted_image_18_lych8z.jpg');

-- 5. Insert Modifiers for some items
INSERT INTO staff.menu_item_modifiers (id, name, menu_item_id) VALUES
(1, 'Choose Meat Preparation', 2), -- Burger
(2, 'Add Extras', 6); -- Nachos

-- 6. Insert Modifier Options
INSERT INTO staff.menu_item_modifier_options (modifier_id, label, price) VALUES
(1, 'Medium Rare', 0.00),
(1, 'Medium', 0.00),
(1, 'Well Done', 0.00),
(2, 'Extra Cheese', 250),
(2, 'Add Chicken', 400);

-- 7. Insert Variants for some items
INSERT INTO staff.menu_item_variants (menu_item_id, label, price) VALUES
(5, 'Regular (10")', 1600),
(5, 'Large (14")', 2200);

-- 8. Insert 1 PENDING (PLACED) Order
INSERT INTO staff.orders (id, property_id, guest_id, guest_name, location, status, total_amount, created_at, updated_at)
VALUES (1001, 1, 1, 'John Doe', 'Room 101', 'PLACED', 1250.00, NOW(), NOW());

INSERT INTO staff.order_items (order_id, menu_item_id, quantity, price_at_order)
VALUES (1001, 1, 1, 1250.00);

-- 9. Insert 1 APPROVED (ACCEPTED) Order
INSERT INTO staff.orders (id, property_id, guest_id, guest_name, location, status, total_amount, created_at, updated_at)
VALUES (1002, 1, 2, 'Jane Smith', 'Room 102', 'ACCEPTED', 2400.00, NOW() - INTERVAL '30 minutes', NOW());

INSERT INTO staff.order_items (order_id, menu_item_id, quantity, price_at_order)
VALUES (1002, 2, 1, 2400.00);

-- 10. Insert 45 COMPLETED (DELIVERED) Orders over the last 14 days
INSERT INTO staff.orders (id, property_id, guest_id, guest_name, location, status, total_amount, created_at, updated_at)
SELECT 
    2000 + gs.id, 
    1, 
    (random() * 100)::int + 10, 
    'Guest ' || (2000 + gs.id), 
    'Room ' || ((random() * 100)::int + 200), 
    'DELIVERED', 
    CASE (gs.id % 5) + 1
        WHEN 1 THEN 1250.00
        WHEN 2 THEN 2400.00
        WHEN 3 THEN 900.00
        WHEN 4 THEN 600.00
        WHEN 5 THEN 1600.00
    END, 
    NOW() - ((random() * 14) || ' days')::interval,
    NOW()
FROM generate_series(1, 45) AS gs(id);

-- Insert 1 Order Item for each of the 45 completed orders
INSERT INTO staff.order_items (order_id, menu_item_id, quantity, price_at_order)
SELECT 
    2000 + gs.id, 
    (gs.id % 5) + 1, 
    1, 
    CASE (gs.id % 5) + 1
        WHEN 1 THEN 1250.00
        WHEN 2 THEN 2400.00
        WHEN 3 THEN 900.00
        WHEN 4 THEN 600.00
        WHEN 5 THEN 1600.00
    END
FROM generate_series(1, 45) AS gs(id);

-- 11. Insert 4 Reviews per Item placeholder and 45 mapped Reviews
INSERT INTO staff.item_reviews (menu_item_id, order_id, guest_name, rating, comment, created_at)
SELECT id, 1001, 'John Doe', 5, 'Absolutely fantastic! Highly recommended.', CURRENT_TIMESTAMP FROM staff.menu_items
UNION ALL
SELECT id, 1001, 'Sarah Smith', 4, 'Very good, but took a little bit long to arrive.', CURRENT_TIMESTAMP FROM staff.menu_items
UNION ALL
SELECT id, 1001, 'Michael B.', 5, 'Tastes just like home. Will order again!', CURRENT_TIMESTAMP FROM staff.menu_items
UNION ALL
SELECT id, 1001, 'Emma Wilson', 4, 'Great portion size and fresh ingredients.', CURRENT_TIMESTAMP FROM staff.menu_items; 

INSERT INTO staff.item_reviews (menu_item_id, order_id, guest_name, rating, comment, created_at)
SELECT 
    (gs.id % 5) + 1, 
    2000 + gs.id, 
    'Guest ' || (2000 + gs.id), 
    (random() * 2 + 3)::int, -- Rating between 3 and 5
    CASE (gs.id % 5) + 1
        WHEN 1 THEN 'The Crispy Calamari was amazing! Perfectly cooked.'
        WHEN 2 THEN 'Burger was huge and very tasty. Highly recommend.'
        WHEN 3 THEN 'Loved the cheesecake, very rich and creamy.'
        WHEN 4 THEN 'Fresh juice, exactly what I needed in the morning.'
        WHEN 5 THEN 'Great pizza, crust was perfect!'
    END,
    (SELECT created_at FROM staff.orders WHERE id = 2000 + gs.id) + INTERVAL '2 hours'
FROM generate_series(1, 45) AS gs(id);
