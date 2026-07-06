package com.beta.loyalty.repository.venue;

import com.beta.loyalty.domain.VenueStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueStaffAssignmentRepository extends JpaRepository<VenueStaffAssignment, UUID> {
    boolean existsByStaffUserIdAndVenueIdAndActiveTrue(UUID staffUserId, UUID venueId);

}
