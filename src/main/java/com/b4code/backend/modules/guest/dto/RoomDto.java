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
}
