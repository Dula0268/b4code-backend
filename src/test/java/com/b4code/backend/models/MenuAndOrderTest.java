package com.b4code.backend.models;

import com.b4code.backend.models.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MenuAndOrderTest {

    @Test
    public void testMenuCreation() {
        Menu menu = new Menu();
        menu.setPropertyId(1L);
        menu.setName("Breakfast Menu");
        menu.setDescription("Morning meals");
        menu.setStatus("active");

        assertEquals(1L, menu.getPropertyId());
        assertEquals("Breakfast Menu", menu.getName());
        assertEquals("Morning meals", menu.getDescription());
        assertEquals("active", menu.getStatus());
    }

    @Test
    public void testOrderCreation() {
        Order order = new Order();
        order.setPropertyId(2L);
        order.setGuestName("Dulanga");
        order.setTotalAmount(45.50);
        order.setStatus(OrderStatus.PLACED);
        order.setItems(new ArrayList<>());

        assertEquals(2L, order.getPropertyId());
        assertEquals("Dulanga", order.getGuestName());
        assertEquals(45.50, order.getTotalAmount());
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertNotNull(order.getItems());
    }

    @Test
    public void testMenuItemUpdate() {
        MenuItem item = new MenuItem();
        item.setName("Burger");
        item.setPrice(new java.math.BigDecimal("10.00"));
        item.setIsAvailable(true);
        
        // Update price and availability
        item.setPrice(new java.math.BigDecimal("12.50"));
        item.setIsAvailable(false);
        
        assertEquals(new java.math.BigDecimal("12.50"), item.getPrice());
        assertFalse(item.getIsAvailable());
    }

    @Test
    public void testMenuItemDeactivation() {
        MenuItem item = new MenuItem();
        item.setName("Pizza");
        item.setIsAvailable(true);
        
        // Deactivate
        item.setIsAvailable(false);
        
        assertFalse(item.getIsAvailable(), "Menu item should be marked as unavailable (deactivated)");
    }
}
