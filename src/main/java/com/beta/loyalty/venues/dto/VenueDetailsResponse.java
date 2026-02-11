package com.beta.loyalty.venues.dto;

import com.beta.loyalty.domain.enums.VenueType;
import com.beta.loyalty.domain.venue.Venue;

import java.util.UUID;

public record VenueDetailsResponse (
        UUID id,
        String name,
        String address,
        String city,
        String country,
        String timezone,
        String type
){
    public static VenueDetailsResponse from(Venue v) {
        return new VenueDetailsResponse(
                v.getId(), v.getName(), v.getAddress(), v.getCity(), v.getCountry(), v.getTimezone(), String.valueOf(v.getType())
        );
    }
}
