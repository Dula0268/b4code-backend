CREATE SCHEMA IF NOT EXISTS app_auth;
CREATE SCHEMA IF NOT EXISTS guest;
CREATE SCHEMA IF NOT EXISTS owner;
CREATE SCHEMA IF NOT EXISTS staff;
CREATE SCHEMA IF NOT EXISTS admin;

-- Clean up tables with schema mismatches before Hibernate migration
DROP TABLE IF EXISTS staff.menu_item_modifier_options CASCADE;
DROP TABLE IF EXISTS staff.menu_item_modifiers CASCADE;
DROP TABLE IF EXISTS staff.menu_item_variants CASCADE;
DROP TABLE IF EXISTS staff.menu_item_images CASCADE;
DROP TABLE IF EXISTS staff.menu_items CASCADE;


