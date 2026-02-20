package com.aventium.identity_service.util;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Id implements Serializable {
    @Column(name = "role_id", nullable = false)
    private UUID roleId;
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

}
