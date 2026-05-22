package com.b4code.backend.modules.admin.dto;

import com.b4code.backend.models.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserStatusUpdateDto {
    private UserStatus status;
}
