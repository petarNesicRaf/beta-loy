package com.beta.loyalty.customer.repository;

import com.beta.loyalty.domain.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUsernameIgnoreCase(String username);

    @Query("""
      select c from Customer c
      where lower(c.username) like lower(concat(:q, '%'))
        and c.status = 'ACTIVE'
    """)
    Page<Customer> searchByUsernamePrefix(@Param("q") String q, Pageable pageable);
}
