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
import com.beta.loyalty.dto.venue.CustomerVenueEnrichedResponse;
import com.beta.loyalty.dto.venue.CustomerVenueEnrichedResponse.NextRewardSummary;
import com.beta.loyalty.dto.venue.VenueDetailsResponse;
import com.beta.loyalty.dto.venue.VenueDetailsWithRewardResponse;
import com.beta.loyalty.dto.venue.VenuePublicResponse;
import com.beta.loyalty.domain.Reward;
import com.beta.loyalty.repository.venue.VenueFavoriteRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerVenueService {
    private final VenueRepository venueRepository;
    private final RewardRepository rewardRepository;
    private final PointsAccountRepository pointsAccountRepository;
    private final VenueFavoriteRepository venueFavoriteRepository;

    @Cacheable(value = "venues", key = "#pageable")
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

//
//    @Transactional(readOnly = true)
//    public VenueDetailsResponse getActiveVenue(UUID venueId) {
//        Venue venue = venueRepository.findByIdAndStatus(venueId, VenueStatus.ACTIVE)
//                .orElseThrow(() -> new NotFoundException("Venue not found"));
//        return VenueDetailsResponse.from(venue);
//    }


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
    public Page<CustomerVenueEnrichedResponse> getMyVenues(Pageable pageable) {
        UUID customerId = CurrentUser.requirePrincipal().userId();

        Page<Venue> venuePage = venueRepository.findAllByStatus(VenueStatus.ACTIVE, pageable);

        if (venuePage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> venueIds = venuePage.map(Venue::getId).getContent();

        // balance per venue (only venues with a PointsAccount are in the map)
        Map<UUID, Long> balanceByVenue = pointsAccountRepository
                .findByCustomer_IdAndCurrentBalanceGreaterThan(customerId, -1L)
                .stream()
                .collect(Collectors.toMap(pa -> pa.getVenue().getId(), pa -> pa.getCurrentBalance()));

        // favorited venue IDs
        Set<UUID> favoriteIds = venueFavoriteRepository.findFavoriteVenueIdsByCustomerId(customerId);

        // active rewards for this page of venues, ordered cheapest first
        Map<UUID, List<Reward>> rewardsByVenue = rewardRepository
                .findActiveByVenueIds(venueIds)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getVenue().getId()));

        return venuePage.map(venue -> {
            long balance = balanceByVenue.getOrDefault(venue.getId(), 0L);
            List<Reward> rewards = rewardsByVenue.getOrDefault(venue.getId(), List.of());

            int redeemableCount = (int) rewards.stream()
                    .filter(r -> r.getPointsCost() <= balance)
                    .count();

            NextRewardSummary nextReward = rewards.stream()
                    .filter(r -> r.getPointsCost() > balance)
                    .findFirst()
                    .map(r -> new NextRewardSummary(r.getId(), r.getName(), r.getPointsCost(), r.getTier()))
                    .orElse(null);

            return new CustomerVenueEnrichedResponse(
                    venue.getId(),
                    venue.getName(),
                    venue.getCity(),
                    venue.getType(),
                    balance,
                    favoriteIds.contains(venue.getId()),
                    redeemableCount,
                    nextReward
            );
        });
    }

    @Cacheable(value = "venue-rewards", key = "{#venueId, #pageable}")
    @Transactional(readOnly = true)
    public Page<RewardBrowseResponse> browseRewards(UUID venueId, Pageable pageable) {
        return rewardRepository.findAllActiveFromActiveVenues(venueId, pageable)
                .map(RewardBrowseResponse::from);
    }
}
