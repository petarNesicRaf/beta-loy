package com.beta.loyalty.dto.reward;

import com.beta.loyalty.domain.Reward;
import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.domain.enums.RewardTier;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RewardPublicResponse(
        UUID id,
        UUID venueId,
        String name,
        String description,
        RewardStatus status,
        long pointsCost,
        long perCustomerLimitPerDay,
        OffsetDateTime validFrom,
        OffsetDateTime validTo,
        Integer stock,
        RewardTier tier
)
{
    public static RewardPublicResponse from(Reward reward) {
        return new RewardPublicResponse(
                reward.getId(),
                reward.getVenue().getId(),
                reward.getName(),
                reward.getDescription(),
                reward.getStatus(),
                reward.getPointsCost(),
                reward.getPerCustomerLimitPerDay(),
                reward.getValidFrom(),
                reward.getValidTo(),
                reward.getStock(),
                reward.getTier()
        );
    }
}
