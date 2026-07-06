package com.beta.loyalty.domain;

import com.beta.loyalty.domain.BaseEntity;
import com.beta.loyalty.domain.Tenant;
import com.beta.loyalty.domain.TenantOwnedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "ux_role_tenant_name", columnNames = {"tenant_id", "name"}))
public class Role extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    Tenant tenant;

    @Column(nullable = false, length = 80)
    String name; // OWNER/MANAGER/CASHIER

}
