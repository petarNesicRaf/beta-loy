package com.beta.loyalty.security;

import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        UserType userType,
        UUID tenantId //null for customer
) {
}
