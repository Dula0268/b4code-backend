package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.MenuItemDto;
import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestMenuService {

    private final MenuItemRepository menuItemRepository;
    
    // Allowed properties for sorting on MenuItem
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "name", "category", "price", "isAvailable", "description"
    );

    public Page<MenuItemDto> getMenuForProperty(Long propertyId, Pageable pageable) {
        Pageable sanitizedPageable = sanitizePageable(pageable);
        Page<MenuItem> items = menuItemRepository.findByPropertyId(propertyId, sanitizedPageable);
        return items.map(this::mapToDto);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> validOrders = pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT_PROPERTIES.contains(order.getProperty()))
                .collect(Collectors.toList());

        if (validOrders.isEmpty()) {
            // If no valid orders, return a default sort or unsorted
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending());
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(validOrders));
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
