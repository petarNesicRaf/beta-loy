package com.beta.loyalty.dto.reward;

import com.beta.loyalty.domain.Reward;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RewardPublicResponse(
        UUID id,
        String name,
        String description,
        long pointsCost,
        OffsetDateTime validFrom,
        OffsetDateTime validTo,
        Integer stock
)
{
    public static RewardPublicResponse from(Reward reward) {
        return new RewardPublicResponse(
                reward.getId(),
                reward.getName(),
                reward.getDescription(),
                reward.getPointsCost(),
                reward.getValidFrom(),
                reward.getValidTo(),
                reward.getStock()
        );
    }
}
