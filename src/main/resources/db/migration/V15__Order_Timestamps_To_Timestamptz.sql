-- orders.created_at/updated_at/cancelled_at/refunded_at were TIMESTAMP WITHOUT
-- TIME ZONE, populated via the JVM's LocalDateTime.now(). In production the
-- container clock is UTC, so these columns actually hold UTC instants with no
-- offset marker. The frontend then parsed the offset-less ISO string as
-- browser-local time (Asia/Colombo, UTC+5:30), showing new orders as ~5.5h
-- old. Converting to TIMESTAMPTZ (interpreting existing values as UTC, which
-- is what they already are) makes Jackson serialize an explicit "Z" offset,
-- so the frontend parses them correctly regardless of browser timezone.
ALTER TABLE staff.orders
    ALTER COLUMN created_at   TYPE TIMESTAMPTZ USING created_at   AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at   TYPE TIMESTAMPTZ USING updated_at   AT TIME ZONE 'UTC',
    ALTER COLUMN cancelled_at TYPE TIMESTAMPTZ USING cancelled_at AT TIME ZONE 'UTC',
    ALTER COLUMN refunded_at  TYPE TIMESTAMPTZ USING refunded_at  AT TIME ZONE 'UTC';
