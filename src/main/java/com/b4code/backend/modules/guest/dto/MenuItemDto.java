package com.b4code.backend.modules.guest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Data Transfer Object for Menu Item details")
public class MenuItemDto {
    @Schema(description = "Unique identifier of the menu item", example = "1")
    private Long id;

    @Schema(description = "Property ID this item belongs to", example = "1")
    private Long propertyId;

    @Schema(description = "Name of the item", example = "Spicy Tuna Roll")
    private String name;

    @Schema(description = "Frontend-friendly title", example = "Spicy Tuna Roll")
    private String title;

    @Schema(description = "Category (e.g. Sushi, Drinks)", example = "Sushi")
    private String category;

    @Schema(description = "Description of the item", example = "Fresh tuna with spicy mayo")
    private String description;

    @Schema(description = "Price of the item", example = "15.50")
    private BigDecimal price;

    @Schema(description = "Price in LKR", example = "4500.00")
    private BigDecimal priceLkr;

    @Schema(description = "Whether the item is currently available", example = "true")
    private Boolean isAvailable;

    @Schema(description = "List of image URLs for the item")
    private List<String> imageUrls;

    @Schema(description = "Primary image URL", example = "http://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "Tag for highlighting (e.g. VEG, HOT)", example = "POPULAR")
    private String tag;
}
