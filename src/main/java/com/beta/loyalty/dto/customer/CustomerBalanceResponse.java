package com.beta.loyalty.dto.customer;

import java.util.UUID;

public record CustomerBalanceResponse(
        UUID customerId,
        UUID venueId,
        long balance
) {}
