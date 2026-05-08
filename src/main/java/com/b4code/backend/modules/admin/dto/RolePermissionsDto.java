package com.b4code.backend.modules.admin.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionsDto {
    private Map<String, List<PermissionDto>> permissions;
}
