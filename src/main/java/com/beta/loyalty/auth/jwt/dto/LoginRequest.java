package com.beta.loyalty.auth.jwt.dto;

public record LoginRequest(
        String identifier,
        String password
) {
}
