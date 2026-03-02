package com.beta.loyalty.venues.repository;

import com.beta.loyalty.domain.staff.VenueStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueStaffAssignmentRepository extends JpaRepository<VenueStaffAssignment, UUID> {
    boolean existsByStaffUserIdAndVenueIdAndActiveTrue(UUID staffUserId, UUID venueId);

}
