package com.beta.loyalty.dto.friends;

import com.beta.loyalty.domain.enums.FriendshipStatus;

import java.util.UUID;

public record CustomerSearchItemDto(
        UUID id,
        String username,
        String displayName,
        FriendshipStatus relationshipStatus, // null if none
        boolean outgoingRequest,             // true if relationshipStatus==PENDING and requestedByMe
        boolean incomingRequest              // true if relationshipStatus==PENDING and requestedByOther
) {
}


