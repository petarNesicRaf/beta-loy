package com.beta.loyalty.venues.controller;

import com.beta.loyalty.venues.dto.VenueDetailsWithRewardResponse;
import com.beta.loyalty.venues.dto.VenuePublicResponse;
import com.beta.loyalty.venues.service.CustomerVenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer/venues")
@RequiredArgsConstructor
public class CustomerVenueController {
    private final CustomerVenueService customerVenueService;

    @GetMapping
    public Page<VenuePublicResponse> list(Pageable pageable) {
        return customerVenueService.listActiveVenues(pageable);
    }

    @GetMapping("/search")
    public List<VenuePublicResponse> search(@RequestParam("q") String q){
        return customerVenueService.searchByName(q);
    }

    @GetMapping("/{venueId}/rewards")
    public VenueDetailsWithRewardResponse getVenueDetails(@PathVariable UUID venueId, Pageable pageable){
        return customerVenueService.getVenueDetails(venueId);
    }
}
