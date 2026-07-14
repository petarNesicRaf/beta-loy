package com.beta.loyalty.dto.venue;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignStaffRequest(
        @NotNull UUID staffUserId
) {
}
