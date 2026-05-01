package com.b4code.backend.modules.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {
    private Long id;
    
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    @NotBlank(message = "Item name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;
    
    private Boolean isAvailable;
}
