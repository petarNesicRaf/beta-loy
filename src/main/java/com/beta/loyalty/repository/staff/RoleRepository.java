package com.beta.loyalty.repository.staff;

import com.beta.loyalty.domain.Role;
import com.beta.loyalty.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByTenantAndName(Tenant tenant, String name);
}
