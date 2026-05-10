package com.b4code.backend.modules.staff.controller;

import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import com.b4code.backend.infrastructure.storage.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
@Tag(name = "Staff: Menu Management", description = "Endpoints for staff to manage property menus and items")
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get all menu items for a property", description = "Returns a list of all dishes/items regardless of category")
    @ApiResponse(responseCode = "200", description = "List of menu items retrieved successfully")
    public ResponseEntity<List<MenuItem>> getMenuItemsByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(menuItemRepository.findByPropertyId(propertyId));
    }

    @PostMapping
    @Operation(summary = "Create a new menu item", description = "Adds a new dish to the property inventory")
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem menuItem) {
        return ResponseEntity.ok(menuItemRepository.save(menuItem));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing menu item", description = "Modify details, price, category, or images of an item")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItemDetails) {
        return menuItemRepository.findById(id).map(item -> {
            item.setName(menuItemDetails.getName());
            item.setDescription(menuItemDetails.getDescription());
            item.setPrice(menuItemDetails.getPrice());
            item.setCategory(menuItemDetails.getCategory());
            item.setIsAvailable(menuItemDetails.getIsAvailable());
            if (menuItemDetails.getImageUrls() != null && !menuItemDetails.getImageUrls().isEmpty()) {
                item.setImageUrls(menuItemDetails.getImageUrls());
            }
            return ResponseEntity.ok(menuItemRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a menu item", description = "Permanently removes an item from the system")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle item availability", description = "Quickly switch item between Available/Unavailable (86'd)")
    public ResponseEntity<MenuItem> toggleMenuItem(@PathVariable Long id) {
        return menuItemRepository.findById(id).map(item -> {
            item.setIsAvailable(!item.getIsAvailable());
            return ResponseEntity.ok(menuItemRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/property/{propertyId}/category/{category}")
    @Operation(summary = "Delete all items in a category", description = "Removes an entire menu category for a property")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long propertyId, @PathVariable String category) {
        List<MenuItem> items = menuItemRepository.findByPropertyIdAndCategory(propertyId, category);
        menuItemRepository.deleteAll(items);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "Upload item image", description = "Uploads a dish photo to Cloudinary and adds to item's image list")
    public ResponseEntity<?> uploadItemImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return menuItemRepository.findById(id).map(item -> {
            try {
                String url = cloudinaryService.uploadImage(file, "menu_items");
                item.getImageUrls().add(url);
                menuItemRepository.save(item);
                return ResponseEntity.ok(Map.of("imageUrl", url, "message", "Image uploaded successfully"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of("error", "Upload failed", "message", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
