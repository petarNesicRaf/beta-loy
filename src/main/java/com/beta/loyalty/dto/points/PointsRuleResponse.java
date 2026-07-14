package com.beta.loyalty.dto.points;

import com.beta.loyalty.domain.PointsRule;
import com.beta.loyalty.domain.enums.RoundingMode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PointsRuleResponse(
        UUID id,
        UUID venueId,
        BigDecimal baseFactor,
        RoundingMode roundingMode,
        int claimWindowMinutes,
        int maxRecipientsPerReceipt,
        OffsetDateTime activeFrom,
        OffsetDateTime activeTo
) {
    public static PointsRuleResponse from(PointsRule r) {
        return new PointsRuleResponse(
                r.getId(),
                r.getVenue().getId(),
                r.getBaseFactor(),
                r.getRoundingMode(),
                r.getClaimWindowMinutes(),
                r.getMaxRecipientsPerReceipt(),
                r.getActiveFrom(),
                r.getActiveTo()
        );
    }
}
