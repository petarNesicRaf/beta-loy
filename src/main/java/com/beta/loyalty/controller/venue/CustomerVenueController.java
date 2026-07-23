package com.beta.loyalty.controller.venue;

import com.beta.loyalty.domain.enums.VenueType;

import java.util.List;
import com.beta.loyalty.dto.reward.RewardBrowseResponse;
import com.beta.loyalty.dto.venue.CustomerVenueEnrichedResponse;
import com.beta.loyalty.dto.venue.VenueDetailsWithRewardResponse;
import com.beta.loyalty.dto.venue.VenuePublicResponse;
import com.beta.loyalty.service.venue.CustomerVenueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer/venues")
@Tag(
        name = "Customer Venue",
        description = "Listanje, pretraga i detalji lokala (sa zaradjenim poenima - progres bar) "
)
@RequiredArgsConstructor
public class CustomerVenueController {
    private final CustomerVenueService customerVenueService;

    @GetMapping("/types")
    public List<VenueType> types() {
        return List.of(VenueType.values());
    }

    @GetMapping
    public Page<VenuePublicResponse> list(Pageable pageable) {
        return customerVenueService.listActiveVenues(pageable);
    }

    @GetMapping("/search")
    public Page<VenuePublicResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Set<VenueType> types,
            Pageable pageable) {
        return customerVenueService.search(q, types, pageable);
    }

    @GetMapping("/{venueId}/rewards")
    public VenueDetailsWithRewardResponse getVenueDetails(@PathVariable UUID venueId){
        return customerVenueService.getVenueDetails(venueId);
    }

    @GetMapping("/mine")
    public Page<CustomerVenueEnrichedResponse> myVenues(Pageable pageable) {
        return customerVenueService.getMyVenues(pageable);
    }

    @GetMapping("/rewards")
    public Page<RewardBrowseResponse> browseRewards(
            @RequestParam(required = false) UUID venueId,
            Pageable pageable) {
        return customerVenueService.browseRewards(venueId, pageable);
    }
}
