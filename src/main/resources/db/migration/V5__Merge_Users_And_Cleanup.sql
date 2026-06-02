-- Drop obsolete room_id column from users table
ALTER TABLE app_auth.users DROP COLUMN IF EXISTS room_id;

-- Add admin-specific columns to unified users table
ALTER TABLE app_auth.users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;
ALTER TABLE app_auth.users ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Drop obsolete staff_properties table
DROP TABLE IF EXISTS staff.staff_properties CASCADE;

-- Merge admin_users data into users table (if admin_users exists)
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'admin' AND table_name = 'admin_users') THEN
        INSERT INTO app_auth.users (
            first_name, 
            last_name, 
            email, 
            password_hash, 
            role, 
            status, 
            last_login, 
            created_at, 
            updated_at, 
            deleted
        )
        SELECT 
            first_name, 
            last_name, 
            email, 
            password_hash, 
            role, 
            status, 
            last_login, 
            created_at, 
            updated_at, 
            deleted
        FROM admin.admin_users
        ON CONFLICT (email) DO NOTHING;
        
        -- Drop the now merged admin_users table
        DROP TABLE IF EXISTS admin.admin_users CASCADE;
    END IF;
END $$;
