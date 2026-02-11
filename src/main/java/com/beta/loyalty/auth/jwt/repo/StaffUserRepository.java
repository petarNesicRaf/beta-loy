package com.beta.loyalty.auth.jwt.repo;

import com.beta.loyalty.domain.staff.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffUserRepository extends JpaRepository<StaffUser, UUID> {
    Optional<StaffUser> findByEmailIgnoreCase(String email);
    Optional<StaffUser> findByPhone(String phone);
}
