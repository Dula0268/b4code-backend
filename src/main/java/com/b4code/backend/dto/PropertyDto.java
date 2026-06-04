package com.b4code.backend.dto;

import com.b4code.backend.models.Property;
import lombok.*;

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
        return property;
    }
}
