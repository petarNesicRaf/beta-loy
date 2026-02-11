package com.beta.loyalty.auth;

import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        UserType userType,
        UUID tenantId //null for customer
) {
}
