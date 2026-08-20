CREATE SCHEMA IF NOT EXISTS app_auth;
CREATE SCHEMA IF NOT EXISTS guest;
CREATE SCHEMA IF NOT EXISTS owner;
CREATE SCHEMA IF NOT EXISTS staff;
CREATE SCHEMA IF NOT EXISTS admin;

-- Clean up tables with schema mismatches before Hibernate migration removed to prevent data loss.
