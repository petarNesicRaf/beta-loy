package com.beta.loyalty.dto.customer;

public record CustomerStatsResponse(
        long totalPoints,
        long venuesCount,
        long rewardsRedeemed
) {}
