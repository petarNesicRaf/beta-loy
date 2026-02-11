package com.beta.loyalty.auth;

import java.util.List;
import java.util.UUID;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String userType,
        UUID tenantId,
        List<String> roles
) {
}
