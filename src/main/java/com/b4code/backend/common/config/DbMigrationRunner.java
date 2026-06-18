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
        log.info("✅ DB migration fixes complete.");
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
}

