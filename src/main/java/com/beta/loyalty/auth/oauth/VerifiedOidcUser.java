package com.beta.loyalty.auth.oauth;

public record VerifiedOidcUser(
        String subject,   // OIDC "sub" - stable per provider+app
        String email,     // may be null in some cases
        String fullName   // optional
) {
}
