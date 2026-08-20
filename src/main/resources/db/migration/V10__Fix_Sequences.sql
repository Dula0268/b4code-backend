SELECT setval('staff.menus_id_seq', COALESCE((SELECT MAX(id) FROM staff.menus), 1));
SELECT setval('staff.menu_categories_id_seq', COALESCE((SELECT MAX(id) FROM staff.menu_categories), 1));
SELECT setval('staff.menu_items_id_seq', COALESCE((SELECT MAX(id) FROM staff.menu_items), 1));
SELECT setval('staff.menu_item_modifiers_id_seq', COALESCE((SELECT MAX(id) FROM staff.menu_item_modifiers), 1));
SELECT setval('staff.orders_id_seq', COALESCE((SELECT MAX(id) FROM staff.orders), 1));
SELECT setval('staff.item_reviews_id_seq', COALESCE((SELECT MAX(id) FROM staff.item_reviews), 1));
SELECT setval('staff.order_items_id_seq', COALESCE((SELECT MAX(id) FROM staff.order_items), 1));
