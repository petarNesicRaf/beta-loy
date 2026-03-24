package com.beta.loyalty.staff.repository;

import com.beta.loyalty.domain.staff.Role;
import com.beta.loyalty.domain.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByTenantAndName(Tenant tenant, String name);
}
