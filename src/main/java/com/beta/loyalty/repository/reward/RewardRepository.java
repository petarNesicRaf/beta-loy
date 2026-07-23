package com.beta.loyalty.repository.reward;

import com.beta.loyalty.domain.Reward;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.domain.enums.VenueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardRepository extends JpaRepository<Reward, UUID> {
    Page<Reward> findAllByVenueId(UUID venueId, Pageable pageable);

    @Query("select r from Reward r where r.venue.id = :venueId and r.venue.tenant.id = :tenantId")
    Page<Reward> findAllByVenueIdAndTenantId(@Param("venueId") UUID venueId, @Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("select r from Reward r where r.id = :id and r.venue.tenant.id = :tenantId")
    Optional<Reward> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("select r from Reward r where r.id = :rewardId and r.venue.id = :venueId")
    Optional<Reward> findByIdAndVenueId(@Param("rewardId") UUID rewardId, @Param("venueId") UUID venueId);

    boolean existsByVenueId(UUID id);

    @Modifying
    @Query("""
        update Reward r
        set r.stock = r.stock - 1
        where r.id = :rewardId
          and r.stock is not null
          and r.stock > 0
    """)
    int decrementStockIfAvailable(@Param("rewardId") UUID rewardId);

    Optional<Reward> findByIdAndStatus(UUID id, RewardStatus status);

    @Query("""
        select r from Reward r
        where r.status = com.beta.loyalty.domain.enums.RewardStatus.ACTIVE
          and r.venue.status = com.beta.loyalty.domain.enums.VenueStatus.ACTIVE
          and (:venueId is null or r.venue.id = :venueId)
        order by r.pointsCost asc
    """)
    Page<Reward> findAllActiveFromActiveVenues(@Param("venueId") UUID venueId, Pageable pageable);

    @Query("""
        select r from Reward r
        where r.status = com.beta.loyalty.domain.enums.RewardStatus.ACTIVE
          and r.venue.id in :venueIds
        order by r.pointsCost asc
    """)
    List<Reward> findActiveByVenueIds(@Param("venueIds") Collection<UUID> venueIds);

}
