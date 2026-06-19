package com.b4code.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MenuItemRequest {
    private Long propertyId;
    private Long menuId;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isAvailable;
    private String tag;
    private Integer calories;
    private List<String> imageUrls;
    private List<MenuItemVariantDto> variants;
    private List<MenuItemModifierDto> modifiers;
}
