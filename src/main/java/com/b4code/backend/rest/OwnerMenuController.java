package com.b4code.backend.rest;

import com.b4code.backend.dao.MenuCategoryRepository;
import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.MenuRepository;
import com.b4code.backend.dto.MenuCategoryDto;
import com.b4code.backend.dto.MenuCategoryRequest;
import com.b4code.backend.dto.MenuDto;
import com.b4code.backend.dto.MenuItemDto;
import com.b4code.backend.dto.MenuItemModifierDto;
import com.b4code.backend.dto.MenuItemModifierOptionDto;
import com.b4code.backend.dto.MenuItemRequest;
import com.b4code.backend.dto.MenuItemVariantDto;
import com.b4code.backend.dto.MenuRequest;
import com.b4code.backend.models.Menu;
import com.b4code.backend.models.MenuCategory;
import com.b4code.backend.models.MenuItem;
import com.b4code.backend.models.MenuItemModifier;
import com.b4code.backend.models.MenuItemModifierOption;
import com.b4code.backend.models.MenuItemVariant;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner/menu")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Owner — Menu", description = "Owner-scoped menu management")
public class OwnerMenuController {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;

    // ─── Menus ───────────────────────────────────────────────────────────────────

    @GetMapping("/property/{propertyId}/menus")
    public ResponseEntity<List<MenuDto>> getMenus(@PathVariable Long propertyId) {
        log.info("Owner: fetching menus for property {}", propertyId);
        List<MenuDto> dtos = menuRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::toMenuDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/menus")
    public ResponseEntity<MenuDto> createMenu(@RequestBody MenuRequest request) {
        log.info("Owner: creating menu '{}' for property {}", request.getName(), request.getPropertyId());
        Menu menu = new Menu();
        menu.setPropertyId(request.getPropertyId());
        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        return ResponseEntity.ok(toMenuDto(menuRepository.save(menu)));
    }

    @PutMapping("/menus/{id}")
    public ResponseEntity<MenuDto> updateMenu(@PathVariable Long id, @RequestBody MenuRequest request) {
        return menuRepository.findById(id)
                .map(menu -> {
                    if (request.getName() != null) menu.setName(request.getName());
                    if (request.getDescription() != null) menu.setDescription(request.getDescription());
                    if (request.getStatus() != null) menu.setStatus(request.getStatus());
                    return ResponseEntity.ok(toMenuDto(menuRepository.save(menu)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/menus/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Categories ──────────────────────────────────────────────────────────────

    @GetMapping("/property/{propertyId}/categories")
    public ResponseEntity<List<MenuCategoryDto>> getCategories(@PathVariable Long propertyId) {
        log.info("Owner: fetching categories for property {}", propertyId);
        List<MenuCategoryDto> dtos = menuCategoryRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/categories")
    public ResponseEntity<MenuCategoryDto> createCategory(@RequestBody MenuCategoryRequest request) {
        log.info("Owner: creating category '{}' for property {}", request.getName(), request.getPropertyId());
        MenuCategory cat = new MenuCategory();
        cat.setPropertyId(request.getPropertyId());
        cat.setName(request.getName());
        return ResponseEntity.ok(toCategoryDto(menuCategoryRepository.save(cat)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        menuCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Menu Items ───────────────────────────────────────────────────────────────

    @GetMapping("/property/{propertyId}/items")
    public ResponseEntity<List<MenuItemDto>> getItems(@PathVariable Long propertyId) {
        log.info("Owner: fetching menu items for property {}", propertyId);
        List<MenuItemDto> dtos = menuItemRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/items")
    public ResponseEntity<MenuItemDto> createItem(@RequestBody MenuItemRequest request) {
        log.info("Owner: creating menu item '{}' for property {}", request.getName(), request.getPropertyId());

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + request.getMenuId()));
        MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));

        MenuItem item = new MenuItem();
        item.setPropertyId(request.getPropertyId());
        item.setMenu(menu);
        item.setCategory(category);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        item.setTag(request.getTag());
        item.setCalories(request.getCalories());
        item.setImageUrls(request.getImageUrls() != null ? request.getImageUrls() : List.of());

        if (request.getVariants() != null) {
            item.setVariants(request.getVariants().stream()
                    .map(v -> new MenuItemVariant(v.getLabel(), v.getPrice()))
                    .collect(Collectors.toList()));
        }

        if (request.getModifiers() != null) {
            List<MenuItemModifier> modifiers = request.getModifiers().stream()
                    .map(m -> {
                        MenuItemModifier modifier = new MenuItemModifier();
                        modifier.setName(m.getName());
                        modifier.setMenuItem(item);
                        if (m.getOptions() != null) {
                            modifier.setOptions(m.getOptions().stream()
                                    .map(o -> new MenuItemModifierOption(o.getLabel(), o.getPrice()))
                                    .collect(Collectors.toList()));
                        }
                        return modifier;
                    })
                    .collect(Collectors.toList());
            item.setModifiers(modifiers);
        }

        return ResponseEntity.ok(toItemDto(menuItemRepository.save(item)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItemDto> updateItem(@PathVariable Long id, @RequestBody MenuItemRequest request) {
        return menuItemRepository.findById(id)
                .map(item -> {
                    if (request.getMenuId() != null) {
                        menuRepository.findById(request.getMenuId()).ifPresent(item::setMenu);
                    }
                    if (request.getCategoryId() != null) {
                        menuCategoryRepository.findById(request.getCategoryId()).ifPresent(item::setCategory);
                    }
                    if (request.getName() != null) item.setName(request.getName());
                    if (request.getDescription() != null) item.setDescription(request.getDescription());
                    if (request.getPrice() != null) item.setPrice(request.getPrice());
                    if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());
                    if (request.getTag() != null) item.setTag(request.getTag());
                    if (request.getCalories() != null) item.setCalories(request.getCalories());
                    if (request.getImageUrls() != null) item.setImageUrls(request.getImageUrls());

                    if (request.getVariants() != null) {
                        item.getVariants().clear();
                        item.getVariants().addAll(request.getVariants().stream()
                                .map(v -> new MenuItemVariant(v.getLabel(), v.getPrice()))
                                .collect(Collectors.toList()));
                    }

                    if (request.getModifiers() != null) {
                        item.getModifiers().clear();
                        item.getModifiers().addAll(request.getModifiers().stream()
                                .map(m -> {
                                    MenuItemModifier modifier = new MenuItemModifier();
                                    modifier.setName(m.getName());
                                    modifier.setMenuItem(item);
                                    if (m.getOptions() != null) {
                                        modifier.setOptions(m.getOptions().stream()
                                                .map(o -> new MenuItemModifierOption(o.getLabel(), o.getPrice()))
                                                .collect(Collectors.toList()));
                                    }
                                    return modifier;
                                })
                                .collect(Collectors.toList()));
                    }

                    return ResponseEntity.ok(toItemDto(menuItemRepository.save(item)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/items/{id}/toggle")
    public ResponseEntity<MenuItemDto> toggleAvailability(@PathVariable Long id) {
        return menuItemRepository.findById(id)
                .map(item -> {
                    item.setIsAvailable(!item.getIsAvailable());
                    return ResponseEntity.ok(toItemDto(menuItemRepository.save(item)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private MenuDto toMenuDto(Menu menu) {
        return MenuDto.builder()
                .id(menu.getId())
                .propertyId(menu.getPropertyId())
                .name(menu.getName())
                .description(menu.getDescription())
                .status(menu.getStatus())
                .build();
    }

    private MenuCategoryDto toCategoryDto(MenuCategory cat) {
        return MenuCategoryDto.builder()
                .id(cat.getId())
                .propertyId(cat.getPropertyId())
                .name(cat.getName())
                .build();
    }

    private MenuItemDto toItemDto(MenuItem item) {
        String imageUrl = (item.getImageUrls() != null && !item.getImageUrls().isEmpty())
                ? item.getImageUrls().get(0) : null;
        return MenuItemDto.builder()
                .id(item.getId())
                .propertyId(item.getPropertyId())
                .menuId(item.getMenu() != null ? item.getMenu().getId() : null)
                .menuName(item.getMenu() != null ? item.getMenu().getName() : null)
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .name(item.getName())
                .title(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .priceLkr(item.getPrice())
                .isAvailable(item.getIsAvailable())
                .imageUrls(item.getImageUrls())
                .imageUrl(imageUrl)
                .tag(item.getTag())
                .calories(item.getCalories())
                .variants(item.getVariants() != null ? item.getVariants().stream()
                        .map(v -> MenuItemVariantDto.builder().label(v.getLabel()).price(v.getPrice()).build())
                        .collect(Collectors.toList()) : List.of())
                .modifiers(item.getModifiers() != null ? item.getModifiers().stream()
                        .map(m -> MenuItemModifierDto.builder()
                                .id(m.getId())
                                .name(m.getName())
                                .options(m.getOptions() != null ? m.getOptions().stream()
                                        .map(o -> MenuItemModifierOptionDto.builder()
                                                .label(o.getLabel()).price(o.getPrice()).build())
                                        .collect(Collectors.toList()) : List.of())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}
