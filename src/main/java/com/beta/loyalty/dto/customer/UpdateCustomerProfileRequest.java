package com.beta.loyalty.dto.customer;

import jakarta.validation.constraints.Size;

public record UpdateCustomerProfileRequest(
        @Size(max = 120) String displayName,
        @Size(min = 3, max = 32) String username,
        @Size(max = 40) String phone
) {}
