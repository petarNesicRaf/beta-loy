package com.beta.loyalty.security;

public record VerifiedOidcUser(
        String subject,   // OIDC "sub" - stable per provider+app
        String email,     // may be null in some cases
        String fullName   // optional
) {
}
