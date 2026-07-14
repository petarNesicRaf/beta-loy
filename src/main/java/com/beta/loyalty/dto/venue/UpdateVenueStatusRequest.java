package com.beta.loyalty.dto.venue;

import com.beta.loyalty.domain.enums.VenueStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateVenueStatusRequest(
        @NotNull VenueStatus status
) {
}
