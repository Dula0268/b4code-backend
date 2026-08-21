package com.b4code.backend.rest;

import com.b4code.backend.dao.MenuCategoryRepository;
import com.b4code.backend.dto.MenuCategoryDto;
import com.b4code.backend.dto.MenuCategoryRequest;
import com.b4code.backend.models.MenuCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu-categories")
@RequiredArgsConstructor
@Slf4j
public class MenuCategoryController {

    private final MenuCategoryRepository menuCategoryRepository;

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MenuCategoryDto>> getCategoriesByProperty(@PathVariable Long propertyId) {
        log.info("Fetching menu categories for property: {}", propertyId);
        List<MenuCategory> categories = menuCategoryRepository.findByPropertyId(propertyId);
        return ResponseEntity.ok(categories.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<MenuCategoryDto> createCategory(@RequestBody MenuCategoryRequest request) {
        log.info("Creating category '{}' for property: {}", request.getName(), request.getPropertyId());
        MenuCategory category = new MenuCategory();
        category.setPropertyId(request.getPropertyId());
        category.setName(request.getName());
        MenuCategory saved = menuCategoryRepository.save(category);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Deleting menu category id: {}", id);
        menuCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MenuCategoryDto toDto(MenuCategory category) {
        return MenuCategoryDto.builder()
                .id(category.getId())
                .propertyId(category.getPropertyId())
                .name(category.getName())
                .build();
    }
}
