package com.beta.loyalty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRedemptionRequestDto(
        @NotNull UUID rewardId,
        String idempotencyKey,
        @Size(max = 500) String customerNote
) {
}
