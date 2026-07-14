package com.beta.loyalty.dto.venue;

import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.VenueStatus;
import com.beta.loyalty.domain.enums.VenueType;

import java.util.UUID;

public record StaffVenueResponse(
        UUID id,
        UUID tenantId,
        String name,
        String pib,
        String address,
        String city,
        String country,
        String timezone,
        VenueStatus status,
        VenueType type
) {
    public static StaffVenueResponse from(Venue v) {
        return new StaffVenueResponse(
                v.getId(),
                v.getTenant().getId(),
                v.getName(),
                v.getPib(),
                v.getAddress(),
                v.getCity(),
                v.getCountry(),
                v.getTimezone(),
                v.getStatus(),
                v.getType()
        );
    }
}
