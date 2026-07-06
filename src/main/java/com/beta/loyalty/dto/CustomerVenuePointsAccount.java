package com.beta.loyalty.dto;

import java.util.UUID;

public record CustomerVenuePointsAccount(
        UUID venueId,
        String venueName,
        long balance
) {}
