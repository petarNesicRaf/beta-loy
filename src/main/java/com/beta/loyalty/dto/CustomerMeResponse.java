package com.beta.loyalty.dto;

import java.util.UUID;

public record CustomerMeResponse(
        UUID id,
        String email,
        String displayName
//        String avatarUrl
//        OffsetDateTime createdAt
) {
}
