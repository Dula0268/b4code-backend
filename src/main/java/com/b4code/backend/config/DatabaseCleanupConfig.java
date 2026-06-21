package com.b4code.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DatabaseCleanupConfig {

    @Bean
    public CommandLineRunner cleanupOldColumns(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                log.info("Checking for obsolete 'category' column in staff.menu_items...");
                jdbcTemplate.execute("ALTER TABLE staff.menu_items DROP COLUMN IF EXISTS category;");
                log.info("Successfully dropped 'category' column if it existed.");
            } catch (Exception e) {
                log.warn("Failed to drop obsolete 'category' column: {}", e.getMessage());
            }
        };
    }
}
