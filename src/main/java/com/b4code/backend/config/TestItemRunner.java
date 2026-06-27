package com.b4code.backend.config;

import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.MenuRepository;
import com.b4code.backend.dao.MenuCategoryRepository;
import com.b4code.backend.models.MenuItem;
import com.b4code.backend.models.Menu;
import com.b4code.backend.models.MenuCategory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Configuration
@Slf4j
public class TestItemRunner {

    @Bean
    public CommandLineRunner testInsertItem(MenuItemRepository menuItemRepository, MenuRepository menuRepository, MenuCategoryRepository menuCategoryRepository) {
        return args -> {
            try {
                log.info("Testing MenuItem insert...");
                // Find first menu and category to use
                Menu menu = menuRepository.findAll().stream().findFirst().orElse(null);
                MenuCategory category = menuCategoryRepository.findAll().stream().findFirst().orElse(null);
                
                if (menu != null && category != null) {
                    MenuItem item = new MenuItem();
                    item.setName("Auto Test Item");
                    item.setDescription("Testing the fix");
                    item.setPrice(new BigDecimal("100.00"));
                    item.setPropertyId(menu.getPropertyId());
                    item.setMenu(menu);
                    item.setCategory(category);
                    item.setIsAvailable(true);
                    
                    menuItemRepository.save(item);
                    log.info("Successfully inserted test MenuItem with ID: {}", item.getId());
                    
                    // Cleanup
                    menuItemRepository.delete(item);
                    log.info("Successfully cleaned up test MenuItem");
                } else {
                    log.warn("Could not find a menu and category to test with.");
                }
            } catch (Exception e) {
                log.error("Failed to insert MenuItem: {}", e.getMessage(), e);
            }
        };
    }
}
