package com.beta.loyalty.friends.dto;

import java.util.UUID;

public record FriendRequestCreateDto(
        UUID targetCustomerId
) {
}
