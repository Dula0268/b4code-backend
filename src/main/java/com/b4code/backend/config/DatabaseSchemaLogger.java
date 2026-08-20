package com.b4code.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class DatabaseSchemaLogger {

    @Bean
    public CommandLineRunner logSchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                log.info("COLUMNS FOR staff.menu_items:");
                List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_schema = 'staff' AND table_name = 'menu_items';"
                );
                for(Map<String, Object> col : columns) {
                    log.info(" - {}: {} (Nullable: {})", col.get("column_name"), col.get("data_type"), col.get("is_nullable"));
                }
            } catch (Exception e) {
                log.warn("Failed to log schema: {}", e.getMessage());
            }
        };
    }
}
