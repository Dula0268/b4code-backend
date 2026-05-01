package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.util.List;

/**
 * DTOs for Staff module
 */
public class StaffDto {

    @Data
    public static class StaffRequest {
        private Long propertyId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role;
        private String department;
        private String status;
    }

    @Data
    public static class StaffResponse {
        private Long id;
        private Long propertyId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role;
        private String department;
        private String status;
        private String photoUrl;
    }

    @Data
    public static class StaffListResponse {
        private List<StaffResponse> staff;
        private long totalCount;
    }
}
