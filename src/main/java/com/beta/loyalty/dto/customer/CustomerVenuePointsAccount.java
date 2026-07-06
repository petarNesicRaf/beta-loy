package com.beta.loyalty.dto.customer;

import java.util.UUID;

public record CustomerVenuePointsAccount(
        UUID venueId,
        String venueName,
        long balance
) {}
