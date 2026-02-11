package com.beta.loyalty.venues.dto;

import com.beta.loyalty.domain.enums.VenueType;
import com.beta.loyalty.domain.venue.Venue;

import java.util.UUID;

public record VenuePublicResponse(
        UUID id,
        String name,
        String address,
        String city,
        String country,
        String type
) {
    public static VenuePublicResponse from(Venue v){
        return new VenuePublicResponse(
                v.getId(),
                v.getName(),
                v.getAddress(),
                v.getCity(),
                v.getCountry(),
                String.valueOf(v.getType())
        );
    }
}
