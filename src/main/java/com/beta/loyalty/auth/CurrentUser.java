package com.beta.loyalty.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {

    private CurrentUser() {}

    public static Optional<AuthPrincipal> principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();

        Object p = auth.getPrincipal();
        if (p instanceof AuthPrincipal ap) return Optional.of(ap);

        return Optional.empty();
    }

    public static AuthPrincipal requirePrincipal() {
        return principal().orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Unauthenticated"));
    }
}
