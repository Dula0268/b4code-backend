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
        PropertyDetailDto dto = PropertyDetailDto.builder()
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

        RoomDto room1 = RoomDto.builder()
            .id(property.getId() + "-r1")
            .name("Master Suite")
            .maxGuests(4)
            .bedType("King Bed")
            .sqft(450)
            .pricePerNight(8000.0)
            .originalPrice(10000.0)
            .tag("Popular")
            .features(java.util.List.of("WiFi", "Air Conditioning", "Ensuite Bathroom"))
            .imageSrc(property.getImageUrl())
            .build();

        RoomDto room2 = RoomDto.builder()
            .id(property.getId() + "-r2")
            .name("Family Room")
            .maxGuests(6)
            .bedType("2 x Queen")
            .sqft(600)
            .pricePerNight(12000.0)
            .originalPrice(15000.0)
            .tag("Refundable")
            .features(java.util.List.of("Kitchenette", "Free Parking"))
            .imageSrc(property.getImageUrl())
            .build();

        dto.setRooms(java.util.List.of(room1, room2));
        return dto;
    }
}
