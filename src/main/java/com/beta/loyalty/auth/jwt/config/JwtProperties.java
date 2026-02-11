package com.beta.loyalty.auth.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties (
        String issuer,
        String secret,
        long accessTokenSeconds
){}
