package com.beta.loyalty.redemptions.dto;

import java.util.UUID;

public record CreateRedemptionRequestDto(
        UUID rewardId,
        String idempotencyKey,
        String customerNote
) {
}
