package com.beta.loyalty.venues.dto;

import com.beta.loyalty.domain.venue.Venue;
import com.beta.loyalty.rewards.dto.RewardPublicResponse;

import java.util.List;
import java.util.UUID;

public record VenueDetailsWithRewardResponse(
        UUID id,
        String name,
        String address,
        String city,
        long pointBalance,
        List<RewardPublicResponse> rewards
){
    public static VenueDetailsWithRewardResponse from(Venue venue,List<RewardPublicResponse> rewards, long points){
        return new VenueDetailsWithRewardResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCity(),
                points,
                rewards
        );
    }
}