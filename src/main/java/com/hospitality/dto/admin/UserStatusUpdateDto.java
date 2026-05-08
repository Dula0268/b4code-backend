package com.hospitality.dto.admin;

import com.hospitality.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserStatusUpdateDto {
    private UserStatus status;
}
