package com.b4code.backend.service;

import com.b4code.backend.models.MenuItem;
import com.b4code.backend.dto.MenuItemDto;
import com.b4code.backend.dao.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
                .build();
    }
}
