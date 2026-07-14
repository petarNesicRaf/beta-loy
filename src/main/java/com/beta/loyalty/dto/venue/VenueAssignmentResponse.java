package com.beta.loyalty.dto.venue;

import com.beta.loyalty.domain.VenueStaffAssignment;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VenueAssignmentResponse(
        UUID id,
        UUID venueId,
        UUID staffUserId,
        String staffEmail,
        boolean active,
        OffsetDateTime assignedAt
) {
    public static VenueAssignmentResponse from(VenueStaffAssignment a) {
        return new VenueAssignmentResponse(
                a.getId(),
                a.getVenue().getId(),
                a.getStaffUser().getId(),
                a.getStaffUser().getEmail(),
                a.isActive(),
                a.getAssignedAt()
        );
    }
}
