package com.b4code.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPendingResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String propertyName;
    private String status;
    private String role;
    private String registeredAt;
}
