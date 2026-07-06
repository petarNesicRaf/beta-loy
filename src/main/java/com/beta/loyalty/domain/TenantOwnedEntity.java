package com.beta.loyalty.domain;

import com.beta.loyalty.domain.BaseEntity;
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
