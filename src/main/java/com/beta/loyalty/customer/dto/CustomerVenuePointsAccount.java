package com.beta.loyalty.customer.dto;

import java.util.UUID;

public record CustomerVenuePointsAccount(
        UUID venueId,
        String venueName,
        long balance
) {}
