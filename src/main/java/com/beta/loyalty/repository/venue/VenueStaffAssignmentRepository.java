package com.beta.loyalty.repository.venue;

import com.beta.loyalty.domain.VenueStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenueStaffAssignmentRepository extends JpaRepository<VenueStaffAssignment, UUID> {
    boolean existsByStaffUserIdAndVenueIdAndActiveTrue(UUID staffUserId, UUID venueId);
    List<VenueStaffAssignment> findAllByVenueId(UUID venueId);
    Optional<VenueStaffAssignment> findByVenueIdAndStaffUserId(UUID venueId, UUID staffUserId);
}
