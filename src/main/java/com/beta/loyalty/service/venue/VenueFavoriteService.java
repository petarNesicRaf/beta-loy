package com.beta.loyalty.service.venue;

import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.VenueFavorite;
import com.beta.loyalty.dto.venue.VenueFavoriteResponse;
import com.beta.loyalty.exception.ConflictException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.auth.CustomerAuthRepository;
import com.beta.loyalty.repository.points.PointsAccountRepository;
import com.beta.loyalty.repository.venue.VenueFavoriteRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueFavoriteService {

    private final VenueFavoriteRepository favoriteRepository;
    private final VenueRepository venueRepository;
    private final CustomerAuthRepository customerRepository;
    private final PointsAccountRepository pointsAccountRepository;

    @Transactional
    public VenueFavoriteResponse addFavorite(UUID customerId, UUID venueId) {
        if (favoriteRepository.existsByCustomer_IdAndVenue_Id(customerId, venueId)) {
            throw new ConflictException("Venue is already in favorites");
        }
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
        Customer customerRef = customerRepository.getReferenceById(customerId);

        VenueFavorite fav = new VenueFavorite();
        fav.setCustomer(customerRef);
        fav.setVenue(venue);
        favoriteRepository.save(fav);

        long balance = pointsAccountRepository.findBalance(customerId, venueId).orElse(0L);
        return VenueFavoriteResponse.from(venue, balance);
    }

    @Transactional
    public void removeFavorite(UUID customerId, UUID venueId) {
        VenueFavorite fav = favoriteRepository.findByCustomer_IdAndVenue_Id(customerId, venueId)
                .orElseThrow(() -> new NotFoundException("Venue is not in favorites"));
        favoriteRepository.delete(fav);
    }

    @Transactional(readOnly = true)
    public List<VenueFavoriteResponse> listFavorites(UUID customerId) {
        return favoriteRepository.findByCustomer_Id(customerId).stream()
                .map(fav -> {
                    long balance = pointsAccountRepository
                            .findBalance(customerId, fav.getVenue().getId())
                            .orElse(0L);
                    return VenueFavoriteResponse.from(fav.getVenue(), balance);
                })
                .toList();
    }
}
