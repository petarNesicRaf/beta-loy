package com.beta.loyalty.redemptions.dto;

import com.beta.loyalty.domain.enums.RedemptionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RedemptionRequestDto(
        UUID id,
        UUID venueId,
        UUID rewardId,
        UUID customerId,
        RedemptionStatus status,
        long pointsCostSnapshot,
        OffsetDateTime requestedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime approvedAt,
        OffsetDateTime rejectedAt,
        OffsetDateTime fulfilledAt
) {}
