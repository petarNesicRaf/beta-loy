package com.beta.loyalty.dto.customer;

import com.beta.loyalty.domain.Customer;

import java.util.UUID;

public record CustomerMeResponse(
        UUID id,
        String email,
        String username,
        String displayName,
        String phone
) {
    public static CustomerMeResponse from(Customer c) {
        return new CustomerMeResponse(
                c.getId(),
                c.getEmail(),
                c.getUsername(),
                c.getDisplayName(),
                c.getPhone()
        );
    }
}
