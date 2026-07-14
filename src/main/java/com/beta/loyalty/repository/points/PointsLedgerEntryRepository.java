package com.beta.loyalty.repository.points;

import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.PointsLedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PointsLedgerEntryRepository extends JpaRepository<PointsLedgerEntry, UUID> {
    boolean existsBySourceTypeAndSourceIdAndCustomerId(LedgerSourceType sourceType, UUID sourceId, UUID customerId);

    @Query("""
        select e from PointsLedgerEntry e
        where e.customer.id = :customerId
          and (:venueId is null or e.venue.id = :venueId)
        order by e.createdAt desc
    """)
    Page<PointsLedgerEntry> findByCustomerIdAndOptionalVenue(
            @Param("customerId") UUID customerId,
            @Param("venueId") UUID venueId,
            Pageable pageable
    );
}
