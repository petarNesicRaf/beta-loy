package com.beta.loyalty.friends.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendRequestCreateDto(
        @NotNull UUID targetCustomerId
) {
}
