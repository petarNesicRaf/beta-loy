package com.beta.loyalty.dto.venue;

import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.dto.reward.RewardPublicResponse;

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