package com.beta.loyalty.rewards.rewards;

import com.beta.loyalty.domain.reward.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardRepository extends JpaRepository<Reward, UUID> {
    Page<Reward> findAllByVenueId(UUID venueId, Pageable pageable);
    boolean existsByVenueId(UUID id);
}
