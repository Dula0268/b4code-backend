package com.b4code.backend.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Runs before DataSeeder (@Order(1) vs @Order(2)) to apply DB-level
 * fixes that Hibernate ddl-auto:update cannot handle automatically,
 * such as dropping NOT NULL constraints from legacy columns that
 * exist in the DB but are not required by the entity mappings.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DbMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("🔧 Running DB migration fixes...");
        dropAllLegacyNotNullConstraints();
        dropDisputesFkConstraints();
        ensureStaffRolesAndPermissions();
        ensureDefaultOwnerBankAccount();
        log.info("✅ DB migration fixes complete.");
    }

    private void ensureStaffRolesAndPermissions() {
        try {
            // 1. Ensure staff_role column exists in users table
            jdbcTemplate.execute("ALTER TABLE app_auth.users ADD COLUMN IF NOT EXISTS staff_role VARCHAR(255);");
            log.info("✅ Ensured staff_role column exists in app_auth.users");

            // 2. Seed default permissions for Kitchen Staff, Property Staff, Staff Admin
            jdbcTemplate.execute("""
                INSERT INTO app_auth.role_permissions (role_name, section, permission_key, label, description, enabled)
                VALUES 
                  ('Kitchen Staff', 'user', 'order_management', 'Order Management', 'Manage guest food orders', true),
                  ('Kitchen Staff', 'user', 'menu_management', 'Menu Management', 'Add and edit menu items', true),
                  ('Kitchen Staff', 'user', 'qr_management', 'QR Management', 'Generate and print QR codes', true),
                  ('Kitchen Staff', 'user', 'guest_messages', 'Guest Messages', 'Communicate with guests', true),
                  ('Kitchen Staff', 'user', 'analytics', 'Analytics Dashboard', 'View property performance metrics', false),
                  ('Kitchen Staff', 'user', 'reviews', 'Review Management', 'View and respond to guest reviews', false)
                ON CONFLICT (role_name, permission_key) DO NOTHING;
            """);

            jdbcTemplate.execute("""
                INSERT INTO app_auth.role_permissions (role_name, section, permission_key, label, description, enabled)
                VALUES 
                  ('Property Staff', 'user', 'order_management', 'Order Management', 'Manage guest food orders', false),
                  ('Property Staff', 'user', 'menu_management', 'Menu Management', 'Add and edit menu items', false),
                  ('Property Staff', 'user', 'qr_management', 'QR Management', 'Generate and print QR codes', false),
                  ('Property Staff', 'user', 'guest_messages', 'Guest Messages', 'Communicate with guests', true),
                  ('Property Staff', 'user', 'analytics', 'Analytics Dashboard', 'View property performance metrics', true),
                  ('Property Staff', 'user', 'reviews', 'Review Management', 'View and respond to guest reviews', true)
                ON CONFLICT (role_name, permission_key) DO NOTHING;
            """);

            jdbcTemplate.execute("""
                INSERT INTO app_auth.role_permissions (role_name, section, permission_key, label, description, enabled)
                VALUES 
                  ('Staff Admin', 'user', 'order_management', 'Order Management', 'Manage guest food orders', true),
                  ('Staff Admin', 'user', 'menu_management', 'Menu Management', 'Add and edit menu items', true),
                  ('Staff Admin', 'user', 'qr_management', 'QR Management', 'Generate and print QR codes', true),
                  ('Staff Admin', 'user', 'guest_messages', 'Guest Messages', 'Communicate with guests', true),
                  ('Staff Admin', 'user', 'analytics', 'Analytics Dashboard', 'View property performance metrics', true),
                  ('Staff Admin', 'user', 'reviews', 'Review Management', 'View and respond to guest reviews', true)
                ON CONFLICT (role_name, permission_key) DO NOTHING;
            """);

            log.info("✅ Seeded default permissions for staff sub-roles");
        } catch (Exception e) {
            log.warn("⚠ Could not configure staff roles and permissions: {}", e.getMessage());
        }
    }

    /**
     * Drops all foreign key constraints from the admin.disputes table.
     * This is needed because guestId and other fields are mapped as simple Long
     * columns in the Dispute entity, but legacy DB schemas might enforce strict
     * FK constraints, causing seeder failures when dummy IDs are used.
     */
    private void dropDisputesFkConstraints() {
        try {
            jdbcTemplate.execute("""
                DO $$
                DECLARE r RECORD;
                BEGIN
                  FOR r IN
                    SELECT conname FROM pg_constraint
                    WHERE conrelid = 'admin.disputes'::regclass
                    AND contype = 'f'
                  LOOP
                    EXECUTE 'ALTER TABLE admin.disputes DROP CONSTRAINT IF EXISTS ' || r.conname;
                  END LOOP;
                END $$;
            """);
            log.info("✅ Dropped all FK constraints from admin.disputes");
        } catch (Exception e) {
            log.warn("⚠ Could not drop FK constraints on admin.disputes: {}", e.getMessage());
        }
    }

    /**
     * Drops NOT NULL from all non-PK columns in owner, admin, guest schemas.
     * This is safe and idempotent — running on an already-nullable column
     * is a no-op in PostgreSQL.
     */
    private void dropAllLegacyNotNullConstraints() {
        String findColumnsQuery = """
            SELECT c.table_schema, c.table_name, c.column_name
            FROM information_schema.columns c
            LEFT JOIN (
                SELECT kcu.table_schema, kcu.table_name, kcu.column_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON tc.constraint_name = kcu.constraint_name
                 AND tc.table_schema    = kcu.table_schema
                WHERE tc.constraint_type = 'PRIMARY KEY'
            ) pk ON c.table_schema = pk.table_schema
                AND c.table_name   = pk.table_name
                AND c.column_name  = pk.column_name
            WHERE c.table_schema IN ('owner', 'admin', 'guest')
              AND c.is_nullable = 'NO'
              AND pk.column_name IS NULL
            """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(findColumnsQuery);
        int count = 0;
        for (Map<String, Object> row : rows) {
            String schema = (String) row.get("table_schema");
            String table  = (String) row.get("table_name");
            String col    = (String) row.get("column_name");
            try {
                jdbcTemplate.execute(
                    "ALTER TABLE " + schema + "." + table +
                    " ALTER COLUMN " + col + " DROP NOT NULL"
                );
                log.debug("  ✔ Dropped NOT NULL: {}.{}.{}", schema, table, col);
                count++;
            } catch (Exception e) {
                log.warn("  ⚠ Could not drop NOT NULL on {}.{}.{}: {}", schema, table, col, e.getMessage());
            }
        }
        if (count > 0) {
            log.info("✅ Dropped NOT NULL from {} column(s) in owner/admin/guest schemas", count);
        } else {
            log.info("✅ No NOT NULL constraints to drop (all clean)");
        }
    }

    private void ensureDefaultOwnerBankAccount() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS owner.bank_accounts (
                    id BIGSERIAL PRIMARY KEY,
                    owner_id BIGINT NOT NULL,
                    bank_name VARCHAR(255) NOT NULL,
                    account_holder VARCHAR(255) NOT NULL,
                    account_number VARCHAR(255) NOT NULL,
                    branch_code VARCHAR(255),
                    is_primary BOOLEAN DEFAULT true,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Seed bank accounts for owners that don't have one yet
            jdbcTemplate.execute("""
                INSERT INTO owner.bank_accounts (owner_id, bank_name, account_holder, account_number, branch_code, is_primary, created_at)
                SELECT u.id,
                       CASE (u.id % 4)
                           WHEN 0 THEN 'Bank of Ceylon'
                           WHEN 1 THEN 'Commercial Bank'
                           WHEN 2 THEN 'Sampath Bank'
                           WHEN 3 THEN 'Hatton National Bank'
                       END,
                       u.first_name || ' ' || u.last_name,
                       '00' || LPAD(u.id::text, 6, '0') || '45891',
                       CASE (u.id % 4)
                           WHEN 0 THEN 'COL-001'
                           WHEN 1 THEN 'CMB-042'
                           WHEN 2 THEN 'SMP-015'
                           WHEN 3 THEN 'HNB-007'
                       END,
                       true,
                       NOW()
                FROM app_auth.users u
                WHERE u.role = 'OWNER'
                  AND u.deleted = false
                  AND NOT EXISTS (SELECT 1 FROM owner.bank_accounts ba WHERE ba.owner_id = u.id)
            """);
            log.info("✅ Ensured bank accounts for owner users");

            // Seed PENDING payout records for owners that have properties but no pending payouts
            jdbcTemplate.execute("""
                INSERT INTO owner.payouts (owner_id, owner_name, property_id, property_name, amount, hotel_amount, food_amount, commission_amount, commission_rate, currency, status, requested_at, processed_at)
                SELECT p.owner_id,
                       p.owner_name,
                       p.id,
                       p.name,
                       75000.00,
                       60000.00,
                       15000.00,
                       NULL,
                       NULL,
                       'LKR',
                       'PENDING',
                       NOW() - INTERVAL '2 days',
                       NOW() - INTERVAL '2 days'
                FROM owner.properties p
                WHERE p.owner_id IS NOT NULL
                  AND p.status = 'APPROVED'
                  AND NOT EXISTS (
                      SELECT 1 FROM owner.payouts pay
                      WHERE pay.property_id = p.id
                        AND pay.status = 'PENDING'
                  )
                LIMIT 3
            """);
            log.info("✅ Seeded PENDING payout records for testing");

        } catch (Exception e) {
            log.warn("⚠ Could not check owner bank accounts table: {}", e.getMessage());
        }
    }
}

