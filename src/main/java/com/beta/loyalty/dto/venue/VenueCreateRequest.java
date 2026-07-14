package com.beta.loyalty.dto.venue;

import com.beta.loyalty.domain.enums.VenueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VenueCreateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 30)  String pib,
        @Size(max = 400) String address,
        @Size(max = 120) String city,
        @Size(max = 80)  String country,
        @Size(max = 80)  String timezone,
        VenueType type
) {
}
