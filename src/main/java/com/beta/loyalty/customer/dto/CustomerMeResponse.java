package com.beta.loyalty.customer.dto;

import java.util.UUID;

public record CustomerMeResponse(
        UUID id,
        String email,
        String displayName
//        String avatarUrl
//        OffsetDateTime createdAt
) {
}
