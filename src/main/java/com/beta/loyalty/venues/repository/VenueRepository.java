package com.beta.loyalty.venues.repository;

import com.beta.loyalty.domain.enums.VenueStatus;
import com.beta.loyalty.domain.tenant.Tenant;
import com.beta.loyalty.domain.venue.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
    Optional<Venue> findByTenantAndNameIgnoreCase(Tenant tenant, String name);
    Page<Venue> findAllByStatus(VenueStatus status, Pageable pageable);
    Page<Venue> findByNameContainingIgnoreCaseAndStatus(String name, VenueStatus status,Pageable pageable);
    Optional<Venue> findByIdAndStatus(UUID id, VenueStatus status);
    boolean existsByIdAndStatus(UUID id, VenueStatus status);
}
