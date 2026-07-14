package com.beta.loyalty.dto.onboarding;

import com.beta.loyalty.domain.enums.VenueType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record OnboardingRequest(
        @NotNull @Valid BusinessInfo business,
        @NotNull @Valid VenueInfo venue
) {
    public record BusinessInfo(
            @NotBlank @Size(max = 200) String name,
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank @Size(max = 30) String pib
    ) {}

    public record VenueInfo(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 400) String address,
            @Size(max = 120) String city,
            @Size(max = 80) String country,
            @Size(max = 80) String timezone,
            VenueType type
    ) {}
}
