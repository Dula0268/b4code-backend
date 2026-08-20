package com.b4code.backend.dto;

import com.b4code.backend.models.RolePermission;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDto {

    private String key;
    private String label;
    private String description;
    private boolean enabled;

    public static PermissionDto fromEntity(RolePermission p) {
        return PermissionDto.builder()
                .key(p.getPermissionKey())
                .label(p.getLabel())
                .description(p.getDescription())
                .enabled(p.isEnabled())
                .build();
    }
}

