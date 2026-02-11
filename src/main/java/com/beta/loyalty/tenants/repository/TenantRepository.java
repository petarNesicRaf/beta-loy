package com.beta.loyalty.tenants.repository;

import com.beta.loyalty.domain.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByNameIgnoreCase(String name);
}
