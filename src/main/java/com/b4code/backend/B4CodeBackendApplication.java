package com.b4code.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class B4CodeBackendApplication {

    public static void main(String[] args) {
        // Load .env variables into System properties for Spring to pick up
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        preInitializeDatabase();

        SpringApplication.run(B4CodeBackendApplication.class, args);
    }

    private static void preInitializeDatabase() {
        String host = System.getProperty("DB_HOST");
        String port = System.getProperty("DB_PORT");
        String name = System.getProperty("DB_NAME");
        String sslMode = System.getProperty("DB_SSLMODE", "require");
        String user = System.getProperty("DB_USER");
        String password = System.getProperty("DB_PASSWORD");

        if (host == null || port == null || name == null || user == null || password == null) {
            System.err.println("Database configuration environment variables missing!");
            return;
        }

        String url = String.format("jdbc:postgresql://%s:%s/%s?sslmode=%s", host, port, name, sslMode);
        System.out.println("Connecting to database for pre-initialization...");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found: " + e.getMessage());
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // 1. owner.rooms name column
            boolean roomsTableExists = false;
            try (ResultSet rs = conn.getMetaData().getTables(null, "owner", "rooms", null)) {
                if (rs.next()) {
                    roomsTableExists = true;
                }
            }

            if (roomsTableExists) {
                stmt.execute("ALTER TABLE owner.rooms ADD COLUMN IF NOT EXISTS name VARCHAR(255);");
                
                // Backfill from room_type if present
                boolean roomTypeExists = false;
                try (ResultSet rs = conn.getMetaData().getColumns(null, "owner", "rooms", "room_type")) {
                    if (rs.next()) {
                        roomTypeExists = true;
                    }
                }
                if (roomTypeExists) {
                    stmt.execute("UPDATE owner.rooms SET name = room_type WHERE name IS NULL;");
                } else {
                    stmt.execute("UPDATE owner.rooms SET name = 'Standard Room' WHERE name IS NULL;");
                }
                System.out.println("Pre-emptively created and backfilled owner.rooms.name.");
            }

            // 2. guest.bookings created_at and status columns
            boolean bookingsTableExists = false;
            try (ResultSet rs = conn.getMetaData().getTables(null, "guest", "bookings", null)) {
                if (rs.next()) {
                    bookingsTableExists = true;
                }
            }

            if (bookingsTableExists) {
                stmt.execute("ALTER TABLE guest.bookings ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;");
                stmt.execute("UPDATE guest.bookings SET created_at = NOW() WHERE created_at IS NULL;");

                stmt.execute("ALTER TABLE guest.bookings ADD COLUMN IF NOT EXISTS status VARCHAR(255);");
                stmt.execute("UPDATE guest.bookings SET status = 'COMPLETED' WHERE status IS NULL;");
                System.out.println("Pre-emptively created and backfilled guest.bookings columns.");
            }

        } catch (Exception e) {
            System.err.println("Database pre-initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

