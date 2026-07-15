package com.beta.loyalty.repository.redemption;

import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.RedemptionRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RedemptionRequestRepository extends JpaRepository<RedemptionRequest, UUID> {
    Optional<RedemptionRequest> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rr from RedemptionRequest rr where rr.id = :id")
    Optional<RedemptionRequest> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
        select rr from RedemptionRequest rr
        where rr.venue.id = :venueId and rr.status = :status
        order by rr.requestedAt desc
    """)
    List<RedemptionRequest> findByVenueAndStatus(UUID venueId, RedemptionStatus status);

    @Query("""
    select count(rr) from RedemptionRequest rr
    where rr.customer.id = :customerId
      and rr.reward.id = :rewardId
      and rr.status in (:statuses)
      and rr.approvedAt >= :from
      and rr.approvedAt < :to""")
    long countApprovedOrFulfilledInRange(
            @Param("customerId") UUID customerId,
            @Param("rewardId") UUID rewardId,
            @Param("statuses") List<RedemptionStatus> statuses,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
    select rr from RedemptionRequest rr
    where rr.venue.id = :venueId
      and rr.status = :status
      and rr.expiresAt > CURRENT_TIMESTAMP
    order by rr.requestedAt desc""")
    List<RedemptionRequest> findActivePendingByVenue(
            UUID venueId,
            RedemptionStatus status
    );

    @Query("""
    select rr from RedemptionRequest rr
    where rr.venue.id        = :venueId
      and rr.venue.tenant.id = :tenantId
      and (:status is null or rr.status = :status)
      and (:from   is null or rr.requestedAt >= :from)
      and (:to     is null or rr.requestedAt <= :to)
    order by rr.requestedAt desc
    """)
    Page<RedemptionRequest> findHistory(
            @Param("venueId")  UUID venueId,
            @Param("tenantId") UUID tenantId,
            @Param("status")   RedemptionStatus status,
            @Param("from")     OffsetDateTime from,
            @Param("to")       OffsetDateTime to,
            Pageable pageable
    );

    @Query("""
    select rr from RedemptionRequest rr
    where rr.id              = :id
      and rr.venue.id        = :venueId
      and rr.venue.tenant.id = :tenantId
    """)
    Optional<RedemptionRequest> findByIdAndVenueIdAndTenantId(
            @Param("id")       UUID id,
            @Param("venueId")  UUID venueId,
            @Param("tenantId") UUID tenantId
    );

    @Query("""
    select rr from RedemptionRequest rr
    where rr.customer.id = :customerId
      and rr.venue.id    = :venueId
    order by rr.requestedAt desc
    """)
    Page<RedemptionRequest> findByCustomerIdAndVenueId(
            @Param("customerId") UUID customerId,
            @Param("venueId")    UUID venueId,
            Pageable pageable
    );

    @Query("""
    select rr from RedemptionRequest rr
    where rr.customer.id = :customerId
    order by rr.requestedAt desc
    """)
    Page<RedemptionRequest> findByCustomerId(
            @Param("customerId") UUID customerId,
            Pageable pageable
    );

    @Query("""
    select rr from RedemptionRequest rr
    where rr.id = :id and rr.customer.id = :customerId
    """)
    Optional<RedemptionRequest> findByIdAndCustomerId(
            @Param("id")         UUID id,
            @Param("customerId") UUID customerId
    );

    @Query("SELECT COUNT(rr) FROM RedemptionRequest rr WHERE rr.customer.id = :customerId AND rr.status = :status")
    long countByCustomerIdAndStatus(
            @Param("customerId") UUID customerId,
            @Param("status")     RedemptionStatus status
    );
}
