package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.MenuItemDto;
import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        return MenuItemDto.builder()
                .id(item.getId())
                .propertyId(item.getPropertyId())
                .name(item.getName())
                .title(item.getName()) // Mapping name to title
                .category(item.getCategory())
                .description(item.getDescription())
                .price(item.getPrice())
                .priceLkr(item.getPrice()) // Mapping price to priceLkr
                .isAvailable(item.getIsAvailable())
                .imageUrls(item.getImageUrls())
                .imageUrl(item.getImageUrls() != null && !item.getImageUrls().isEmpty() ? item.getImageUrls().get(0) : null)
                .build();
    }
}
