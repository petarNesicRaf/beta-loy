package com.beta.loyalty.repository.auth;

import com.beta.loyalty.domain.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffAuthRepository extends JpaRepository<StaffUser, UUID> {
    Optional<StaffUser> findByEmailIgnoreCase(String email);
    Optional<StaffUser> findByPhone(String phone);
}
