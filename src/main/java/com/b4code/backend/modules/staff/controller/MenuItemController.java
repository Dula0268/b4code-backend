package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Staff: Menu Management", description = "Endpoints for staff to manage property menu items")
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get all menu items for a property")
    public ResponseEntity<List<MenuItem>> getMenuItems(@PathVariable Long propertyId) {
        log.info("Fetching menu items for property: {}", propertyId);
        return ResponseEntity.ok(menuItemRepository.findByPropertyId(propertyId));
    }

    @GetMapping("/staff/properties/{propertyId}/menus")
    @Operation(summary = "Get menu items for staff context")
    public ResponseEntity<List<MenuItem>> getStaffPropertyMenus(@PathVariable Long propertyId) {
        return getMenuItems(propertyId);
    }

    @GetMapping("/staff/properties/{propertyId}/menus/{category}")
    @Operation(summary = "Get menu items by category")
    public ResponseEntity<List<MenuItem>> getStaffPropertyMenusByCategory(
            @PathVariable Long propertyId, 
            @PathVariable String category) {
        log.info("Fetching menu items for property: {} category: {}", propertyId, category);
        return ResponseEntity.ok(menuItemRepository.findByPropertyIdAndCategory(propertyId, category));
    }

    @PostMapping
    @Operation(summary = "Create a new menu item")
    @ApiResponse(responseCode = "200", description = "Menu item created")
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem menuItem) {
        log.info("Creating menu item: {}", menuItem.getName());
        return ResponseEntity.ok(menuItemRepository.save(menuItem));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing menu item")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem details) {
        return menuItemRepository.findById(id)
                .map(item -> {
                    item.setName(details.getName());
                    item.setDescription(details.getDescription());
                    item.setPrice(details.getPrice());
                    item.setCategory(details.getCategory());
                    item.setIsAvailable(details.getIsAvailable());
                    item.setImageUrls(details.getImageUrls());
                    return ResponseEntity.ok(menuItemRepository.save(item));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle menu item availability")
    public ResponseEntity<MenuItem> toggleAvailability(@PathVariable Long id) {
        return menuItemRepository.findById(id)
                .map(item -> {
                    item.setIsAvailable(!item.getIsAvailable());
                    return ResponseEntity.ok(menuItemRepository.save(item));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/property/{propertyId}/category/{category}")
    @Transactional
    @Operation(summary = "Delete all menu items in a category")
    public ResponseEntity<Void> deleteByCategory(@PathVariable Long propertyId, @PathVariable String category) {
        log.info("Deleting items for property {} in category {}", propertyId, category);
        menuItemRepository.deleteByPropertyIdAndCategory(propertyId, category);
        return ResponseEntity.noContent().build();
    }
}
