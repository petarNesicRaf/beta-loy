package com.beta.loyalty.repository.venue;

import com.beta.loyalty.domain.VenueFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface VenueFavoriteRepository extends JpaRepository<VenueFavorite, UUID> {
    List<VenueFavorite> findByCustomer_Id(UUID customerId);
    Optional<VenueFavorite> findByCustomer_IdAndVenue_Id(UUID customerId, UUID venueId);
    boolean existsByCustomer_IdAndVenue_Id(UUID customerId, UUID venueId);

    @Query("SELECT f.venue.id FROM VenueFavorite f WHERE f.customer.id = :customerId")
    Set<UUID> findFavoriteVenueIdsByCustomerId(@Param("customerId") UUID customerId);
}
