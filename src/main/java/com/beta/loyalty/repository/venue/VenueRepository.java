package com.beta.loyalty.repository.venue;

import com.beta.loyalty.domain.enums.VenueStatus;
import com.beta.loyalty.domain.enums.VenueType;
import com.beta.loyalty.domain.Tenant;
import com.beta.loyalty.domain.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
    Optional<Venue> findByTenantAndNameIgnoreCase(Tenant tenant, String name);
    Page<Venue> findAllByStatus(VenueStatus status, Pageable pageable);
    Optional<Venue> findByIdAndStatus(UUID id, VenueStatus status);
    boolean existsByIdAndStatus(UUID id, VenueStatus status);
    Optional<Venue> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<Venue> findAllByTenantId(UUID tenantId, Pageable pageable);

    @Query("""
        select v from Venue v
        where v.status = 'ACTIVE'
          and (:q is null or lower(v.name) like lower(concat('%', :q, '%')))
          and v.type in :types
        order by v.name
    """)
    Page<Venue> search(
            @Param("q") String q,
            @Param("types") Collection<VenueType> types,
            Pageable pageable
    );
}
