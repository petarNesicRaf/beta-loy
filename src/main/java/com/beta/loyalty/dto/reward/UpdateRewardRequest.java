package com.beta.loyalty.dto.reward;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

public record UpdateRewardRequest(

        @NotBlank
        @NotNull
        @Size(max = 160)
        String name,

        String description,

        @Min(1)
        Long pointsCost,

        OffsetDateTime validFrom,

        @Future
        OffsetDateTime validTo,

        @Min(0)
        Integer stock,

        @Min(1)
        Integer perCustomerLimitPerDay
) {
}
