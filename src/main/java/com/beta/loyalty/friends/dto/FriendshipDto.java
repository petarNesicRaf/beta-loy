package com.beta.loyalty.friends.dto;

import com.beta.loyalty.domain.enums.FriendshipStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FriendshipDto(
        UUID id,
        UUID otherCustomerId,
        String otherUsername,
        String otherDisplayName,
        FriendshipStatus status,
        boolean requestedByMe,
        OffsetDateTime respondedAt
) {
}
