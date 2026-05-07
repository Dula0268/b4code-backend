package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MenuItem>> getByProperty(@PathVariable Long propertyId) {
        List<MenuItem> items = menuItemRepository.findByPropertyId(propertyId);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MenuItem menuItem) {
        Map<String, String> errors = validateMenuItem(menuItem);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        if (menuItem.getIsAvailable() == null) {
            menuItem.setIsAvailable(true);
        }
        if (menuItem.getCategory() == null || menuItem.getCategory().isBlank()) {
            menuItem.setCategory("General");
        }

        MenuItem saved = menuItemRepository.save(menuItem);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MenuItem menuItem) {
        MenuItem existing = menuItemRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (menuItem.getName() != null && menuItem.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("name", "Name must not be blank"));
        }
        if (menuItem.getPrice() != null && menuItem.getPrice() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("price", "Price must be greater than zero"));
        }

        if (menuItem.getName() != null) existing.setName(menuItem.getName());
        if (menuItem.getDescription() != null) existing.setDescription(menuItem.getDescription());
        if (menuItem.getPrice() != null) existing.setPrice(menuItem.getPrice());
        if (menuItem.getCategory() != null) existing.setCategory(menuItem.getCategory());
        if (menuItem.getIsAvailable() != null) existing.setIsAvailable(menuItem.getIsAvailable());

        MenuItem saved = menuItemRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id) {
        MenuItem existing = menuItemRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setIsAvailable(!Boolean.TRUE.equals(existing.getIsAvailable()));
        MenuItem saved = menuItemRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!menuItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        menuItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @DeleteMapping("/property/{propertyId}/category/{category}")
    public ResponseEntity<Void> deleteByCategory(@PathVariable Long propertyId, @PathVariable String category) {
        menuItemRepository.deleteByPropertyIdAndCategory(propertyId, category);
        return ResponseEntity.noContent().build();
    }

    private Map<String, String> validateMenuItem(MenuItem item) {
        Map<String, String> errors = new HashMap<>();
        if (item.getName() == null || item.getName().isBlank()) {
            errors.put("name", "Name is required");
        }
        if (item.getPrice() == null || item.getPrice() <= 0) {
            errors.put("price", "Price must be greater than zero");
        }
        if (item.getPropertyId() == null) {
            errors.put("propertyId", "Property ID is required");
        }
        return errors;
    }
}