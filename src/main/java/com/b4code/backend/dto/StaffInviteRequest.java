package com.b4code.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffInviteRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Long propertyId;
}
