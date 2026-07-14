package com.beta.loyalty.repository.points;

import com.beta.loyalty.domain.PointsRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PointsRuleRepository extends JpaRepository<PointsRule, UUID> {

    Page<PointsRule> findAllByVenueId(UUID venueId, Pageable pageable);

    @Query("select r from PointsRule r where r.id = :id and r.venue.tenant.id = :tenantId")
    Optional<PointsRule> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
        select r from PointsRule r
        where r.venue.id = :venueId
          and (r.activeFrom is null or r.activeFrom <= :now)
          and (r.activeTo   is null or r.activeTo   >  :now)
        order by r.activeFrom desc
        limit 1
    """)
    Optional<PointsRule> findActiveByVenueId(@Param("venueId") UUID venueId, @Param("now") OffsetDateTime now);
}
