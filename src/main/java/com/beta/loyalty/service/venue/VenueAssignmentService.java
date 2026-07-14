package com.beta.loyalty.service.venue;

import com.beta.loyalty.domain.StaffUser;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.VenueStaffAssignment;
import com.beta.loyalty.dto.venue.AssignStaffRequest;
import com.beta.loyalty.dto.venue.VenueAssignmentResponse;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.staff.StaffUserRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueAssignmentService {
    private final VenueRepository venueRepository;
    private final StaffUserRepository staffUserRepository;
    private final VenueStaffAssignmentRepository assignmentRepository;

    @Transactional
    public VenueAssignmentResponse assign(UUID tenantId, UUID venueId, AssignStaffRequest req) {
        Venue venue = findVenueForTenant(tenantId, venueId);
        StaffUser staff = staffUserRepository.findByIdAndTenantId(req.staffUserId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Staff member not found"));

        var existing = assignmentRepository.findByVenueIdAndStaffUserId(venueId, req.staffUserId());

        if (existing.isPresent()) {
            VenueStaffAssignment assignment = existing.get();
            if (!assignment.isActive()) {
                assignment.setActive(true);
                assignment.setAssignedAt(OffsetDateTime.now());
            }
            return VenueAssignmentResponse.from(assignment);
        }

        VenueStaffAssignment assignment = new VenueStaffAssignment();
        assignment.setVenue(venue);
        assignment.setStaffUser(staff);
        assignment.setActive(true);
        assignment.setAssignedAt(OffsetDateTime.now());

        return VenueAssignmentResponse.from(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<VenueAssignmentResponse> listAssignments(UUID tenantId, UUID venueId) {
        findVenueForTenant(tenantId, venueId);
        return assignmentRepository.findAllByVenueId(venueId).stream()
                .map(VenueAssignmentResponse::from)
                .toList();
    }

    @Transactional
    public void removeAssignment(UUID tenantId, UUID venueId, UUID staffUserId) {
        findVenueForTenant(tenantId, venueId);

        VenueStaffAssignment assignment = assignmentRepository
                .findByVenueIdAndStaffUserId(venueId, staffUserId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        assignment.setActive(false);
    }

    private Venue findVenueForTenant(UUID tenantId, UUID venueId) {
        return venueRepository.findByIdAndTenantId(venueId, tenantId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
    }
}
