package com.beta.loyalty.dto.venue;

import com.beta.loyalty.domain.enums.RewardTier;
import com.beta.loyalty.domain.enums.VenueType;

import java.util.UUID;

public record CustomerVenueEnrichedResponse(
        UUID venueId,
        String venueName,
        String city,
        VenueType type,
        long pointsBalance,
        boolean isFavorite,
        int redeemableRewardsCount,
        NextRewardSummary nextReward
) {
    public record NextRewardSummary(
            UUID id,
            String name,
            long pointsCost,
            RewardTier tier
    ) {}
}
