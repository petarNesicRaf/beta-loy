package com.beta.loyalty.dto.friends;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendRequestCreateDto(
        @NotNull UUID targetCustomerId
) {
}
