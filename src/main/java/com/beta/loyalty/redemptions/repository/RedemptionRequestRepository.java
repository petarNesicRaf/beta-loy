package com.beta.loyalty.redemptions.repository;

import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.redemption.RedemptionRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
