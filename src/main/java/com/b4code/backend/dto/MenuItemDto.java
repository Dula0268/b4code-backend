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
    private String name;
    private String title; // Mapping name to title for frontend
    private String category;
    private String description;
    private BigDecimal price;
    private BigDecimal priceLkr; // Mapping price to priceLkr for frontend
    private Boolean isAvailable;
    private List<String> imageUrls;
    private String imageUrl; // Single image URL for compatibility
    private String tag; // POPULAR, VEG, etc.
}

