package com.beta.loyalty.dto.venue;

import com.beta.loyalty.domain.Venue;

import java.util.UUID;

public record VenueFavoriteResponse(
        UUID venueId,
        String name,
        String address,
        String city,
        String type,
        long balance
) {
    public static VenueFavoriteResponse from(Venue v, long balance) {
        return new VenueFavoriteResponse(
                v.getId(),
                v.getName(),
                v.getAddress(),
                v.getCity(),
                v.getType().name(),
                balance
        );
    }
}
