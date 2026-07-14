package com.beta.loyalty.dto.customer;

import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.enums.CustomerStatus;

import java.util.UUID;

public record StaffCustomerResponse(
        UUID id,
        String email,
        String username,
        String displayName,
        String phone,
        CustomerStatus status
) {
    public static StaffCustomerResponse from(Customer c) {
        return new StaffCustomerResponse(
                c.getId(),
                c.getEmail(),
                c.getUsername(),
                c.getDisplayName(),
                c.getPhone(),
                c.getStatus()
        );
    }
}
