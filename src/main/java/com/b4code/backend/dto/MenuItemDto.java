package com.b4code.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {
    private Long id;
    private Long propertyId;

    // Menu reference
    private Long menuId;
    private String menuName;

    // Category reference
    private Long categoryId;
    private String categoryName;

    private String name;
    private String title;       // Alias for name (frontend compatibility)
    private String description;
    private BigDecimal price;
    private BigDecimal priceLkr; // Alias for price (frontend compatibility)
    private Boolean isAvailable;
    private List<String> imageUrls;
    private String imageUrl;     // Single image URL (frontend compatibility)
    private String tag;
    private Integer calories;
    private List<MenuItemVariantDto> variants;
    private List<MenuItemModifierDto> modifiers;
}
