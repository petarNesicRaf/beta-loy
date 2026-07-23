package com.beta.loyalty.dto.reward;

import com.beta.loyalty.domain.enums.RewardTier;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RewardCreateRequest(

        @NotBlank
        @Size(max = 160)
        String name,

        String description,

        @NotNull
        @Min(1)
        Long pointsCost,

        OffsetDateTime validFrom,

        @Future
        OffsetDateTime validTo,

        @Min(0)
        Integer stock,

        @Min(1)
        Integer perCustomerLimitPerDay,

        RewardTier tier
) {
}
