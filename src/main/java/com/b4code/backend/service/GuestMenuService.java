package com.b4code.backend.service;

import com.b4code.backend.models.MenuItem;
import com.b4code.backend.dto.MenuItemDto;
import com.b4code.backend.dto.MenuItemVariantDto;
import com.b4code.backend.dto.MenuItemModifierDto;
import com.b4code.backend.dto.MenuItemModifierOptionDto;
import com.b4code.backend.dao.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestMenuService {

    private final MenuItemRepository menuItemRepository;

    public Page<MenuItemDto> getMenuForProperty(Long propertyId, Pageable pageable) {
        Page<MenuItem> items = menuItemRepository.findByPropertyId(propertyId, pageable);
        return items.map(this::mapToDto);
    }

    public MenuItemDto mapToDto(MenuItem item) {
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
                                        .map(o -> MenuItemModifierOptionDto.builder().label(o.getLabel()).price(o.getPrice()).build())
                                        .collect(Collectors.toList()) : List.of())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}
