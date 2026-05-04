package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.modules.admin.models.Property;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDetailDto {

    private Long id;
    private String title;
    private String location;
    private String fullAddress;
    private String propertyType;
    private Double pricePerNight;
    private Double rating;
    private Integer reviewCount;
    private String badge;
    private String imageSrc;
    private List<String> galleryImages;

    // Host info
    private String hostName;
    private String hostBio;
    private Integer hostYears;
    private Boolean hostSuperhost;

    // Description
    private String description;

    // Amenities
    private List<AmenityDto> amenities;

    // Reviews breakdown
    private List<RatingBreakdownDto> reviewBreakdown;
    
    // Reviews list
    private List<ReviewDetailDto> reviews;

    // Rooms
    private List<RoomDto> rooms;

    // Map coords
    private Double lat;
    private Double lng;

    public static PropertyDetailDto fromEntity(Property property) {
        return PropertyDetailDto.builder()
                .id(property.getId())
                .title(property.getName())
                .location("Sri Lanka")
                .fullAddress("123 Paradise Lane, Colombo, Sri Lanka")
                .propertyType("Villa")
                .pricePerNight(50000.0)
                .rating(4.5)
                .reviewCount(0)
                .imageSrc(property.getImageUrl())
                .hostName(property.getOwnerName())
                .hostBio("Experienced host")
                .hostYears(5)
                .hostSuperhost(false)
                .description("Luxury villa with stunning views")
                .lat(6.9271)
                .lng(80.7789)
                .build();
    }
}
