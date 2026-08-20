package com.b4code.backend.dto;

import lombok.Data;

@Data
public class MenuRequest {
    private Long propertyId;
    private String name;
    private String description;
    private String status;
}
