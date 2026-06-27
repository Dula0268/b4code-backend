package com.b4code.backend.config;

import com.b4code.backend.dao.MenuCategoryRepository;
import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.MenuRepository;
import com.b4code.backend.models.Menu;
import com.b4code.backend.models.MenuCategory;
import com.b4code.backend.models.MenuItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        seedDatabase();
    }

    public void seedDatabase() {
        System.out.println("Starting Database Seeding...");
        
        try {
            jdbcTemplate.execute("UPDATE staff.orders SET status = UPPER(status)");
            jdbcTemplate.execute("UPDATE staff.orders SET status = 'PLACED' WHERE status = 'NEW'");
            jdbcTemplate.execute("UPDATE staff.orders SET status = 'IN_PROGRESS' WHERE status = 'PREPARING'");
            jdbcTemplate.execute("UPDATE staff.orders SET status = 'DELIVERED' WHERE status = 'COMPLETED'");
            System.out.println("Fixed status values in staff.orders");
        } catch (Exception e) {
            System.out.println("Could not update status values: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE staff.orders ALTER COLUMN guest_id DROP NOT NULL");
            System.out.println("Dropped NOT NULL constraint on guest_id in staff.orders");
        } catch (Exception e) {
            System.out.println("Could not drop constraint (might not exist or already dropped): " + e.getMessage());
        }

        Long propertyId = 1L;

        // Check if menus already exist for propertyId 1
        List<Menu> existingMenus = menuRepository.findByPropertyId(propertyId);
        if (!existingMenus.isEmpty()) {
            log.info("Database already seeded with menus for property 1. Skipping seeder.");
            return;
        }

        log.info("Starting database seeder for property 1...");

        // Create 3 Menus
        Menu breakfastMenu = new Menu(null, propertyId, "Breakfast Menu", "Start your day with our delicious breakfast options", "active");
        Menu lunchMenu = new Menu(null, propertyId, "Lunch Menu", "Hearty and fulfilling lunch items", "active");
        Menu dinnerMenu = new Menu(null, propertyId, "Dinner Menu", "Exquisite dinner selections", "active");
        menuRepository.saveAll(List.of(breakfastMenu, lunchMenu, dinnerMenu));

        // Create 3 Categories
        MenuCategory startersCategory = new MenuCategory(null, propertyId, "Starters");
        MenuCategory mainsCategory = new MenuCategory(null, propertyId, "Mains");
        MenuCategory drinksCategory = new MenuCategory(null, propertyId, "Drinks");
        menuCategoryRepository.saveAll(List.of(startersCategory, mainsCategory, drinksCategory));

        // Create 5 Items for Breakfast
        createItem(propertyId, breakfastMenu, startersCategory, "Pancakes", "Fluffy pancakes with maple syrup", new BigDecimal("12.50"));
        createItem(propertyId, breakfastMenu, mainsCategory, "English Breakfast", "Eggs, bacon, sausage, beans, and toast", new BigDecimal("18.00"));
        createItem(propertyId, breakfastMenu, mainsCategory, "Omelette", "Three-egg omelette with cheese and herbs", new BigDecimal("10.00"));
        createItem(propertyId, breakfastMenu, drinksCategory, "Orange Juice", "Freshly squeezed orange juice", new BigDecimal("5.00"));
        createItem(propertyId, breakfastMenu, drinksCategory, "Coffee", "Hot brewed coffee", new BigDecimal("3.50"));

        // Create 5 Items for Lunch
        createItem(propertyId, lunchMenu, startersCategory, "Caesar Salad", "Classic Caesar salad with croutons", new BigDecimal("9.50"));
        createItem(propertyId, lunchMenu, mainsCategory, "Club Sandwich", "Triple-decker sandwich with turkey and bacon", new BigDecimal("14.00"));
        createItem(propertyId, lunchMenu, mainsCategory, "Cheeseburger", "Beef burger with cheddar cheese and fries", new BigDecimal("16.50"));
        createItem(propertyId, lunchMenu, drinksCategory, "Iced Tea", "Refreshing iced tea with lemon", new BigDecimal("4.00"));
        createItem(propertyId, lunchMenu, drinksCategory, "Lemonade", "Chilled homemade lemonade", new BigDecimal("4.50"));

        // Create 5 Items for Dinner
        createItem(propertyId, dinnerMenu, startersCategory, "Bruschetta", "Toasted bread with tomatoes and basil", new BigDecimal("8.00"));
        createItem(propertyId, dinnerMenu, mainsCategory, "Grilled Salmon", "Salmon fillet with asparagus and lemon butter", new BigDecimal("24.00"));
        createItem(propertyId, dinnerMenu, mainsCategory, "Ribeye Steak", "Juicy 10oz ribeye steak with mashed potatoes", new BigDecimal("32.00"));
        createItem(propertyId, dinnerMenu, drinksCategory, "Red Wine", "Glass of house red wine", new BigDecimal("9.00"));
        createItem(propertyId, dinnerMenu, drinksCategory, "Sparkling Water", "Chilled sparkling mineral water", new BigDecimal("3.00"));

        log.info("Database successfully seeded with 3 menus and 15 items for property 1.");
    }

    private void createItem(Long propertyId, Menu menu, MenuCategory category, String name, String description, BigDecimal price) {
        MenuItem item = new MenuItem();
        item.setPropertyId(propertyId);
        item.setMenu(menu);
        item.setCategory(category);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setIsAvailable(true);
        item.setCalories((int) (Math.random() * 500) + 100);
        
        // Use a random sample from Cloudinary demo account
        String[] samples = {"sample.jpg", "food.jpg", "breakfast.jpg", "dessert.jpg"};
        String sample = samples[(int) (Math.random() * samples.length)];
        item.setImageUrls(List.of("https://res.cloudinary.com/demo/image/upload/" + sample));
        
        menuItemRepository.save(item);
    }
}
