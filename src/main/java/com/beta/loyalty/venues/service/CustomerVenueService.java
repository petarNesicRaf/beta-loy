package com.beta.loyalty.venues.service;

import com.beta.loyalty.auth.CurrentUser;
import com.beta.loyalty.common.exceptions.NotFoundException;
import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.domain.enums.VenueStatus;
import com.beta.loyalty.domain.points.PointsAccount;
import com.beta.loyalty.domain.venue.Venue;
import com.beta.loyalty.points.repository.PointsAccountRepository;
import com.beta.loyalty.rewards.dto.RewardPublicResponse;
import com.beta.loyalty.rewards.rewards.RewardRepository;
import com.beta.loyalty.venues.dto.VenueDetailsResponse;
import com.beta.loyalty.venues.dto.VenueDetailsWithRewardResponse;
import com.beta.loyalty.venues.dto.VenuePublicResponse;
import com.beta.loyalty.venues.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerVenueService {
    private final VenueRepository venueRepository;
    private final RewardRepository rewardRepository;
    private final PointsAccountRepository pointsAccountRepository;

    @Transactional(readOnly = true)
    public Page<VenuePublicResponse> listActiveVenues(Pageable pageable) {
        return venueRepository
                .findAllByStatus(VenueStatus.ACTIVE, pageable)
                .map(VenuePublicResponse::from);
    }

    @Transactional(readOnly = true)
    public List<VenuePublicResponse> searchByName(String q){
        if(q == null || q.trim().length()<2){
            return List.of();
        }
        Pageable limit = PageRequest.of(0,10, Sort.by("name"));

        return venueRepository.findByNameContainingIgnoreCaseAndStatus(q.trim(), VenueStatus.ACTIVE,limit)
                .map(VenuePublicResponse::from)
                .getContent();
    }
    @Transactional(readOnly = true)
    public VenueDetailsResponse getActiveVenue(UUID venueId) {
        Venue venue = venueRepository.findByIdAndStatus(venueId, VenueStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
        return VenueDetailsResponse.from(venue);
    }

    @Transactional(readOnly = true)
    public VenueDetailsWithRewardResponse getVenueDetails(UUID venueId){
        UUID currentId = CurrentUser.requirePrincipal().userId();

        Venue venue = venueRepository.findByIdAndStatus(venueId, VenueStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Venue not found"));

        Pageable top5 = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<RewardPublicResponse> rewards = rewardRepository
                .findAllByVenueId(venueId, top5)
                .map(RewardPublicResponse::from)
                .getContent();

        //ako nije earn/spendova vrati da je balance 0
        long pointsBalance = pointsAccountRepository.findBalance(currentId, venueId)
                .orElse(0L);


        return  VenueDetailsWithRewardResponse.from(venue, rewards, pointsBalance);
    }
}
