package com.b4code.backend.modules.guest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {
    private String id;
    private String name;
    private Integer maxGuests;
    private String bedType;
    private Integer sqft;
    private Double pricePerNight;
    private Double originalPrice;
    private String tag;
    private List<String> features;
    private String imageSrc;

    public static RoomDto fromEntity(com.b4code.backend.modules.guest.entity.Room room) {
        if (room == null) return null;
        RoomDto dto = RoomDto.builder()
                .id(room.getId() == null ? null : String.valueOf(room.getId()))
                .name(room.getName())
                .maxGuests(room.getMaxGuests())
                .bedType(room.getBedType())
                .sqft(room.getSqft())
                .pricePerNight(room.getPricePerNight())
                .originalPrice(room.getOriginalPrice())
                .tag(room.getTag())
                .imageSrc(room.getImageUrl())
                .build();

        // parse features if stored as comma-separated
        if (room.getFeatures() != null && !room.getFeatures().isEmpty()) {
            String[] parts = room.getFeatures().split(",");
            dto.setFeatures(java.util.Arrays.stream(parts).map(String::trim).toList());
        }
        return dto;
    }
}
