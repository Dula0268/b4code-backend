package com.b4code.backend.modules.staff.service;

import com.b4code.backend.modules.staff.dto.MenuItemDTO;
import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;

    public MenuItemDTO createMenuItem(MenuItemDTO menuItemDTO) {
        MenuItem menuItem = new MenuItem();
        menuItem.setPropertyId(menuItemDTO.getPropertyId());
        menuItem.setName(menuItemDTO.getName());
        menuItem.setDescription(menuItemDTO.getDescription());
        menuItem.setPrice(menuItemDTO.getPrice());
        menuItem.setIsAvailable(menuItemDTO.getIsAvailable() != null ? menuItemDTO.getIsAvailable() : true);
        
        MenuItem savedItem = menuItemRepository.save(menuItem);
        return convertToDTO(savedItem);
    }

    public MenuItemDTO getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        return convertToDTO(menuItem);
    }

    public List<MenuItemDTO> getMenuItemsByPropertyId(Long propertyId) {
        return menuItemRepository.findByPropertyIdOrderByName(propertyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getAvailableMenuItems(Long propertyId) {
        return menuItemRepository.findByPropertyIdAndIsAvailable(propertyId, true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MenuItemDTO updateMenuItem(Long id, MenuItemDTO menuItemDTO) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        
        if (menuItemDTO.getName() != null) {
            menuItem.setName(menuItemDTO.getName());
        }
        if (menuItemDTO.getDescription() != null) {
            menuItem.setDescription(menuItemDTO.getDescription());
        }
        if (menuItemDTO.getPrice() != null) {
            menuItem.setPrice(menuItemDTO.getPrice());
        }
        if (menuItemDTO.getIsAvailable() != null) {
            menuItem.setIsAvailable(menuItemDTO.getIsAvailable());
        }
        
        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return convertToDTO(updatedItem);
    }

    public MenuItemDTO toggleAvailability(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return convertToDTO(updatedItem);
    }

    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    private MenuItemDTO convertToDTO(MenuItem menuItem) {
        return new MenuItemDTO(
                menuItem.getId(),
                menuItem.getPropertyId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getIsAvailable()
        );
    }
}
