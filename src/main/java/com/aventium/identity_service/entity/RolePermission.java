package com.aventium.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import com.aventium.identity_service.util.Id;



@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role_permission")
public class RolePermission {

    @EmbeddedId
    private Id id = new Id();

    @MapsId("roleId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @MapsId("permissionId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

}
