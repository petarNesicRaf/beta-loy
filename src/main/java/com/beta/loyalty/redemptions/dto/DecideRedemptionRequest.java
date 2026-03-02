package com.beta.loyalty.redemptions.dto;

import com.beta.loyalty.domain.enums.DecisionType;

public record DecideRedemptionRequest(
        DecisionType decision,
        String reason
) {
}
