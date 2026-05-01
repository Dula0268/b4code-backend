package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.dto.MenuItemDTO;
import com.b4code.backend.modules.staff.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemService menuItemService;

    @PostMapping
    public ResponseEntity<MenuItemDTO> createMenuItem(@Valid @RequestBody MenuItemDTO menuItemDTO) {
        MenuItemDTO createdItem = menuItemService.createMenuItem(menuItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long id) {
        MenuItemDTO item = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MenuItemDTO>> getMenuItemsByProperty(@PathVariable Long propertyId) {
        List<MenuItemDTO> items = menuItemService.getMenuItemsByPropertyId(propertyId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/property/{propertyId}/available")
    public ResponseEntity<List<MenuItemDTO>> getAvailableMenuItems(@PathVariable Long propertyId) {
        List<MenuItemDTO> items = menuItemService.getAvailableMenuItems(propertyId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @PathVariable Long id,
            @RequestBody MenuItemDTO menuItemDTO) {
        MenuItemDTO updatedItem = menuItemService.updateMenuItem(id, menuItemDTO);
        return ResponseEntity.ok(updatedItem);
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<MenuItemDTO> toggleAvailability(@PathVariable Long id) {
        MenuItemDTO updatedItem = menuItemService.toggleAvailability(id);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
