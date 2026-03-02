package com.beta.loyalty.staff.repository;

import com.beta.loyalty.domain.staff.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffUserRepository extends JpaRepository<StaffUser, UUID> {

    Optional<StaffUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
