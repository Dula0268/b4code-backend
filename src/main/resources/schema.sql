CREATE SCHEMA IF NOT EXISTS app_auth;
CREATE SCHEMA IF NOT EXISTS guest;
CREATE SCHEMA IF NOT EXISTS owner;
CREATE SCHEMA IF NOT EXISTS staff;
CREATE SCHEMA IF NOT EXISTS admin;

-- Clean up legacy menu_items table if it still has the old 'category' string column
-- This allows Hibernate to correctly recreate the table with the new normalized schema (menu_id, category_id)
DO $$ 
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema='staff' AND table_name='menu_items' AND column_name='category'
  ) THEN
    DROP TABLE IF EXISTS staff.menu_item_images CASCADE;
    DROP TABLE IF EXISTS staff.menu_items CASCADE;
  END IF;
END $$;


