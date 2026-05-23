package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_permissions", schema = "app_auth", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_name", "permission_key"})
})
@Getter
@Setter
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(nullable = false)
    private String section;

    @Column(name = "permission_key", nullable = false)
    private String permissionKey;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean enabled;
}
