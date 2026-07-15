package com.beta.loyalty.service.venue;

import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.domain.enums.VenueStatus;
import com.beta.loyalty.domain.enums.VenueType;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.repository.points.PointsAccountRepository;
import com.beta.loyalty.dto.reward.RewardPublicResponse;
import com.beta.loyalty.repository.reward.RewardRepository;
import com.beta.loyalty.dto.reward.RewardBrowseResponse;
import com.beta.loyalty.dto.venue.VenueDetailsResponse;
import com.beta.loyalty.dto.venue.VenueDetailsWithRewardResponse;
import com.beta.loyalty.dto.venue.VenuePublicResponse;
import com.beta.loyalty.repository.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
    public Page<VenuePublicResponse> search(String q, Set<VenueType> types, Pageable pageable) {
        String normalizedQ = (q == null || q.isBlank()) ? null : q.trim();
        Collection<VenueType> effectiveTypes = (types == null || types.isEmpty())
                ? Arrays.asList(VenueType.values())
                : types;
        return venueRepository.search(normalizedQ, effectiveTypes, pageable)
                .map(VenuePublicResponse::from);
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

    @Transactional(readOnly = true)
    public Page<RewardBrowseResponse> browseRewards(UUID venueId, Pageable pageable) {
        return rewardRepository.findAllActiveFromActiveVenues(venueId, pageable)
                .map(RewardBrowseResponse::from);
    }
}
