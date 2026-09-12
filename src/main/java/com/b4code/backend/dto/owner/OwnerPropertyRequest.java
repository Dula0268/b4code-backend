package com.b4code.backend.dto.owner;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerPropertyRequest {

    private String propertyName;
    private String propertyType;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;

    private List<RoomRequest> rooms;
    private List<String> amenities;
    private String checkInTime;
    private String checkOutTime;
    private String customRules;

    private String coverPhoto;
    private List<String> images;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomRequest {
        private String name;
        private Integer baseCapacity;
        private Integer maxCapacity;
        private String bedConfiguration;
    }
}
