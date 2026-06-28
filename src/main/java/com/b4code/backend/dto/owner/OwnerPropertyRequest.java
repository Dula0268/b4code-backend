package com.b4code.backend.dto.owner;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerPropertyRequest {

    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private String contactPhone;
    private String contactEmail;
    private String checkIn;
    private String checkOut;
    private String houseRules;
    private String propertyType;
    private List<String> amenities;
    private String imageUrl;
}
