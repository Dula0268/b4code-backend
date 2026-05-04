package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.modules.admin.models.Property;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyListingDto {

    private Long id;
    private String title;
    private String location;
    private String propertyType;
    private Double pricePerNight;
    private Integer maxGuests;
    private Integer baseGuests;
    private Double extraGuestFee;
    private Double rating;
    private Integer reviewCount;
    private String badge;
    private String imageSrc;

    public static PropertyListingDto fromEntity(Property property) {
        return PropertyListingDto.builder()
                .id(property.getId())
                .title(property.getName())
                .location("Sri Lanka") // You may need to extend Property entity with location
                .propertyType("Villa") // You may need to extend Property entity
                .pricePerNight(50000.0) // You may need to extend Property entity
                .maxGuests(6)
                .baseGuests(2)
                .extraGuestFee(5000.0)
                .rating(4.5)
                .reviewCount(0)
                .imageSrc(property.getImageUrl())
                .build();
    }
}
