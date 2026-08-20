package com.b4code.backend.dto;

import com.b4code.backend.models.Property;
import com.b4code.backend.models.enums.PropertyStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {
    private Long id;
    private String name;
    private String description;
    private String addressLine1;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private Long ownerId;
    
    // Admin fields
    private String ownerName;
    private LocalDateTime createdAt;
    private PropertyStatus status;
    private String mainImageUrl;
    private Double serviceChargeRate;

    public static PropertyDto fromEntity(Property p) {
        if (p == null) return null;
        return PropertyDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .addressLine1(p.getAddressLine1())
                .city(p.getCity())
                .country(p.getCountry())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .ownerId(p.getOwnerId())
                .createdAt(p.getCreatedAt())
                .status(p.getStatus())
                .serviceChargeRate(p.getServiceChargeRate())
                .build();
    }

    public void updateEntity(Property property) {
        property.setName(this.name);
        property.setDescription(this.description);
        property.setAddressLine1(this.addressLine1);
        property.setCity(this.city);
        property.setCountry(this.country);
        property.setLatitude(this.latitude);
        property.setLongitude(this.longitude);
        property.setOwnerId(this.ownerId);
        if (this.serviceChargeRate != null) {
            property.setServiceChargeRate(this.serviceChargeRate);
        }
        // Do not update createdAt or status here as they are managed by system/admin
    }

    public Property toEntity() {
        Property property = new Property();
        property.setName(this.name);
        property.setDescription(this.description);
        property.setAddressLine1(this.addressLine1);
        property.setCity(this.city);
        property.setCountry(this.country);
        property.setLatitude(this.latitude);
        property.setLongitude(this.longitude);
        property.setOwnerId(this.ownerId);
        if (this.serviceChargeRate != null) {
            property.setServiceChargeRate(this.serviceChargeRate);
        }
        // Default status is set via builder/entity
        return property;
    }
}
