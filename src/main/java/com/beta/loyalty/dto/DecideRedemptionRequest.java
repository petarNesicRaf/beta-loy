package com.beta.loyalty.dto;

import com.beta.loyalty.domain.enums.DecisionType;
import jakarta.validation.constraints.NotNull;

public record DecideRedemptionRequest(
        @NotNull DecisionType decision,
        String reason
) {
}
