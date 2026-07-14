package com.beta.loyalty.repository.staff;

import com.beta.loyalty.domain.StaffUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffUserRepository extends JpaRepository<StaffUser, UUID> {
    Optional<StaffUser> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<StaffUser> findAllByTenantId(UUID tenantId, Pageable pageable);
    Optional<StaffUser> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByEmailAndTenantId(String email, UUID tenantId);
}
