package com.beta.loyalty.dto.points;

import com.beta.loyalty.domain.PointsLedgerEntry;
import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.enums.LedgerType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id,
        UUID venueId,
        String venueName,
        LedgerType type,
        long pointsDelta,
        LedgerSourceType sourceType,
        UUID sourceId,
        OffsetDateTime createdAt
) {
    public static LedgerEntryResponse from(PointsLedgerEntry e) {
        return new LedgerEntryResponse(
                e.getId(),
                e.getVenue().getId(),
                e.getVenue().getName(),
                e.getType(),
                e.getPointsDelta(),
                e.getSourceType(),
                e.getSourceId(),
                e.getCreatedAt()
        );
    }
}
