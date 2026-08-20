package com.b4code.backend.rest;

import com.b4code.backend.dao.ItemReviewRepository;
import com.b4code.backend.dao.MenuCategoryRepository;
import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.MenuRepository;
import com.b4code.backend.dto.MenuItemDto;
import com.b4code.backend.dto.MenuItemRequest;
import com.b4code.backend.dto.MenuItemVariantDto;
import com.b4code.backend.dto.MenuItemModifierDto;
import com.b4code.backend.dto.MenuItemModifierOptionDto;
import com.b4code.backend.models.Menu;
import com.b4code.backend.models.MenuCategory;
import com.b4code.backend.models.MenuItem;
import com.b4code.backend.models.MenuItemVariant;
import com.b4code.backend.models.MenuItemModifier;
import com.b4code.backend.models.MenuItemModifierOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final ItemReviewRepository itemReviewRepository;

    // ─── Guest & Staff: Get all items for a property ─────────────────────────────
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MenuItemDto>> getMenuItems(@PathVariable Long propertyId) {
        log.info("Fetching menu items for property: {}", propertyId);
        List<MenuItem> items = menuItemRepository.findByPropertyId(propertyId);
        return ResponseEntity.ok(toDtos(items));
    }

    // ─── Staff: Get all items for a specific menu ─────────────────────────────────
    @GetMapping("/menu/{menuId}")
    public ResponseEntity<List<MenuItemDto>> getMenuItemsByMenu(@PathVariable Long menuId) {
        log.info("Fetching menu items for menu: {}", menuId);
        List<MenuItem> items = menuItemRepository.findByMenuId(menuId);
        return ResponseEntity.ok(toDtos(items));
    }

    // ─── Staff: Create item ───────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<MenuItemDto> createMenuItem(@RequestBody MenuItemRequest request) {
        log.info("Creating menu item '{}' for property: {}", request.getName(), request.getPropertyId());

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

        MenuItem saved = menuItemRepository.save(item);
        return ResponseEntity.ok(toDto(saved));
    }

    // ─── Staff: Update item ───────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDto> updateMenuItem(@PathVariable Long id, @RequestBody MenuItemRequest request) {
        return menuItemRepository.findById(id)
                .map(item -> {
                    if (request.getMenuId() != null) {
                        menuRepository.findById(request.getMenuId()).ifPresent(menu -> item.setMenu(menu));
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

                    return ResponseEntity.ok(toDto(menuItemRepository.save(item)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Staff: Toggle availability ───────────────────────────────────────────────
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<MenuItemDto> toggleAvailability(@PathVariable Long id) {
        return menuItemRepository.findById(id)
                .map(item -> {
                    item.setIsAvailable(!item.getIsAvailable());
                    return ResponseEntity.ok(toDto(menuItemRepository.save(item)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Staff: Delete single item ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Staff: Delete all items in a menu ────────────────────────────────────────
    @DeleteMapping("/menu/{menuId}")
    @Transactional
    public ResponseEntity<Void> deleteItemsByMenu(@PathVariable Long menuId) {
        log.info("Deleting all items for menu: {}", menuId);
        menuItemRepository.deleteByMenuId(menuId);
        return ResponseEntity.noContent().build();
    }

    // ─── Helper: Map a list of entities to DTOs, with rating summaries batched in one query ──
    private List<MenuItemDto> toDtos(List<MenuItem> items) {
        List<Long> ids = items.stream().map(MenuItem::getId).collect(Collectors.toList());
        java.util.Map<Long, ItemReviewRepository.MenuItemRatingSummary> summaries = ids.isEmpty()
                ? java.util.Map.of()
                : itemReviewRepository.findRatingSummaryByMenuItemIds(ids).stream()
                        .collect(Collectors.toMap(ItemReviewRepository.MenuItemRatingSummary::getMenuItemId, s -> s));
        return items.stream().map(item -> toDto(item, summaries.get(item.getId()))).collect(Collectors.toList());
    }

    // ─── Helper: Map entity to DTO (single item — no rating summary available) ───
    private MenuItemDto toDto(MenuItem item) {
        return toDto(item, null);
    }

    private MenuItemDto toDto(MenuItem item, ItemReviewRepository.MenuItemRatingSummary ratingSummary) {
        String imageUrl = (item.getImageUrls() != null && !item.getImageUrls().isEmpty())
                ? item.getImageUrls().get(0) : null;
        Double avgRating = ratingSummary != null && ratingSummary.getAvgRating() != null
                ? Math.round(ratingSummary.getAvgRating() * 10.0) / 10.0 : null;
        Long reviewCount = ratingSummary != null ? ratingSummary.getReviewCount() : 0L;
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
                                        .map(o -> MenuItemModifierOptionDto.builder().label(o.getLabel()).price(o.getPrice()).build())
                                        .collect(Collectors.toList()) : List.of())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }
}
