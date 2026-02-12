package com.beta.loyalty.auth.jwt.repo;

import com.beta.loyalty.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerAuthRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmailIgnoreCase(String email);
    Optional<Customer> findByPhone(String phone);
}
