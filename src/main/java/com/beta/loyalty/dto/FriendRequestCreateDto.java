package com.beta.loyalty.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendRequestCreateDto(
        @NotNull UUID targetCustomerId
) {
}
