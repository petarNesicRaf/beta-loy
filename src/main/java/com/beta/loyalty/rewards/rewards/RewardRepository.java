package com.beta.loyalty.rewards.rewards;

import com.beta.loyalty.domain.reward.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardRepository extends JpaRepository<Reward, UUID> {
    Page<Reward> findAllByVenueId(UUID venueId, Pageable pageable);

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
}
