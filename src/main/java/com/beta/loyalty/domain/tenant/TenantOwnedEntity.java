package com.beta.loyalty.domain.tenant;

import com.beta.loyalty.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@MappedSuperclass
public abstract class TenantOwnedEntity extends BaseEntity {
    @Column(nullable = false, columnDefinition = "uuid")
    UUID tenantId;
}
