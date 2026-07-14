package com.beta.loyalty.dto.reward;

import com.beta.loyalty.domain.enums.RewardStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRewardStatusRequest(
        @NotNull
        RewardStatus status
) {
}
