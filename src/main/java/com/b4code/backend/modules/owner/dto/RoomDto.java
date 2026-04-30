package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs for Room Management module
 */
public class RoomDto {

    @Data
    public static class RoomRequest {
        private String name;
        private String roomType;
        private Long propertyId;
        private Integer maxAdults;
        private Integer maxChildren;
        private BigDecimal nightlyRate;
        private String currency;
        private String description;
        private String status; // AVAILABLE, MAINTENANCE
    }

    @Data
    public static class RoomResponse {
        private Long id;
        private String name;
        private String roomType;
        private Long propertyId;
        private String status;
        private BigDecimal baseRate;
        private String currency;
        private Integer maxOccupancy;
        private Integer maxAdults;
        private Integer maxChildren;
        private String description;
    }

    @Data
    public static class RoomKpiResponse {
        private int totalRooms;
        private int occupied;
        private int maintenance;
        private int vacant;
        private List<RoomResponse> rooms;
        private int currentPage;
        private int totalPages;
        private long totalItems;
    }
}
