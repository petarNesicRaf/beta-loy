package com.beta.loyalty.dto.onboarding;

import java.util.UUID;

public record OnboardingResponse(
        UUID tenantId,
        String tenantName,
        UUID ownerId,
        String ownerEmail,
        UUID venueId,
        String venueName,
        boolean singleVenue
) {}
