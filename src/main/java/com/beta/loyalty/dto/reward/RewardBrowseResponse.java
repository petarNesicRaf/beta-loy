package com.beta.loyalty.dto.reward;

import com.beta.loyalty.domain.Reward;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RewardBrowseResponse(
        UUID id,
        UUID venueId,
        String venueName,
        String city,
        String name,
        String description,
        long pointsCost,
        Integer stock,
        OffsetDateTime validFrom,
        OffsetDateTime validTo
) {
    public static RewardBrowseResponse from(Reward r) {
        return new RewardBrowseResponse(
                r.getId(),
                r.getVenue().getId(),
                r.getVenue().getName(),
                r.getVenue().getCity(),
                r.getName(),
                r.getDescription(),
                r.getPointsCost(),
                r.getStock(),
                r.getValidFrom(),
                r.getValidTo()
        );
    }
}
